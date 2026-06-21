import json
from typing import Any

from app.agent.core.router import AgentRouteDecision
from app.agent.core.session import public_state_context
from app.agent.schemas import AgentIntent


_LANG_RULE = (
    "Detect the language of the latest user message "
    "and write every user-facing string (answer, question, label) in that same language. "
    "All reasoning and internal fields (reason, missingFields) must be in English."
)

def intent_prompt(message: str, memory: list[dict[str, str]]) -> str:
    return f"""
You are the intent classifier for the CINX learning assistant.
Classify the latest user message into exactly one intent.

<intent_schema>
GREETING | COURSE_SEARCH | POLICY_QA | LEARNING_PATH_PROPOSAL |
LEARNING_PATH_EDIT | LEARNING_PATH_COMMIT | GENERAL_QA |
OUT_OF_SCOPE | CLARIFICATION
</intent_schema>

<flow_mapping>
GREETING, GENERAL_QA         -> direct_answer_flow          -> tools: []
COURSE_SEARCH                -> course_search_flow          -> tools: [course_search, course_get_details]
POLICY_QA                    -> policy_qa_flow              -> tools: [policy_retrieve]
LEARNING_PATH_PROPOSAL       -> learning_path_proposal_flow -> tools: [course_search, course_get_details]
LEARNING_PATH_EDIT           -> learning_path_edit_flow     -> tools: [learning_path_update_proposal, course_get_details, course_search]
LEARNING_PATH_COMMIT         -> learning_path_commit_flow   -> tools: [learning_path_create], requiresConfirmation: true
OUT_OF_SCOPE                 -> scoped_refusal_flow         -> tools: []
CLARIFICATION                -> clarification_flow          -> tools: []
</flow_mapping>

<priority_rules>
P1 (highest): GREETING - any social opening or brief greeting in any language.
P2: OUT_OF_SCOPE - topic unrelated to CINX learning, courses, policy, certificates, or learning paths.
P3: POLICY_QA over COURSE_SEARCH - refund, payment, enrollment rules, certificate conditions, or terms of service.
P4: CLARIFICATION - apply when ANY of these is true:
    - User wants to create/suggest a learning path but no goal, topic, or skill is stated,
      and no prior course/search context exists in conversation.
    - User wants to search courses but no topic, keyword, or filter is stated.
    - User wants to edit a proposal but no specific structural change is described.
    - Intent cannot be safely determined.
    Never infer a missing goal - ask instead.
P5: LEARNING_PATH_COMMIT - ONLY when the user explicitly requests to save, store, or create the proposed learning path into the system database (e.g. using phrases meaning "save path", "save to system", "create path in system", "save it to account", "save this path", or "commit path" in any language).
    - If the user says general affirmative words (like "ok", "agree", "confirm" in any language), it should ONLY be classified as LEARNING_PATH_COMMIT if the immediately preceding assistant message in the conversation history asked the user to confirm saving/creating the path.
    - If the user says "ok" or "agree" but the assistant did not ask to save/commit the path, or if they say phrases meaning "this path is ok now" or "it's done" without explicitly asking to save it into the system, do NOT classify as LEARNING_PATH_COMMIT (classify as LEARNING_PATH_PROPOSAL, LEARNING_PATH_EDIT, or GENERAL_QA instead).
P6: LEARNING_PATH_EDIT - only when the user explicitly describes a structural change (adding/removing items) or metadata change to an existing proposal.
P7: LEARNING_PATH_PROPOSAL - user asks how to progress from a skill to a goal, or asks to suggest/create a path.
</priority_rules>

<few_shot_examples>
Message: "hello!"
-> {{"intent":"GREETING","confidence":0.99,"nextFlow":"direct_answer_flow","allowedTools":[],"requiresConfirmation":false,"question":null,"reason":"Social greeting"}}

Message: "I want to learn something"
-> {{"intent":"CLARIFICATION","confidence":0.91,"nextFlow":"clarification_flow","allowedTools":[],"requiresConfirmation":false,"question":"What topic or skill would you like to learn?","reason":"No topic or goal specified"}}

Message: "Can I get a refund?"
-> {{"intent":"POLICY_QA","confidence":0.97,"nextFlow":"policy_qa_flow","allowedTools":["policy_retrieve"],"requiresConfirmation":false,"question":null,"reason":"Refund question in policy domain"}}

Message: "Suggest a learning path from that keyword"
Conversation has prior course search results.
-> {{"intent":"LEARNING_PATH_PROPOSAL","confidence":0.9,"nextFlow":"learning_path_proposal_flow","allowedTools":["course_search","course_get_details"],"requiresConfirmation":false,"question":null,"reason":"User references prior search context"}}

Message: "Edit the learning path"
-> {{"intent":"CLARIFICATION","confidence":0.86,"nextFlow":"clarification_flow","allowedTools":[],"requiresConfirmation":false,"question":"What course or lesson would you like to add, remove, or reorder in the path?","reason":"Edit request lacks a specific structural change"}}

Message: "Save this learning path to the system for me"
-> {{"intent":"LEARNING_PATH_COMMIT","confidence":0.95,"nextFlow":"learning_path_commit_flow","allowedTools":["learning_path_create"],"requiresConfirmation":true,"question":null,"reason":"User explicitly requested to save/create the learning path into the system"}}

Message: "This learning path is good now"
-> {{"intent":"GENERAL_QA","confidence":0.85,"nextFlow":"direct_answer_flow","allowedTools":[],"requiresConfirmation":false,"question":null,"reason":"User approved the design but did not explicitly request to save or create it in the system"}}
</few_shot_examples>

<language_rule>
{_LANG_RULE}
The "question" field must be written in the same language as the latest user message.
</language_rule>

<conversation>
{json.dumps(memory, ensure_ascii=False)}
</conversation>

<latest_message>
{message}
</latest_message>

Return strict JSON only - no preamble, no markdown fences:
{{
  "intent": "COURSE_SEARCH",
  "confidence": 0.0,
  "nextFlow": "course_search_flow",
  "allowedTools": ["course_search"],
  "requiresConfirmation": false,
  "question": null,
  "reason": "short reason in English"
}}
"""


def actionability_prompt(
    message: str,
    intent: str,
    memory: list[dict[str, str]],
    state: dict[str, Any],
) -> str:
    public = public_state_context(state)
    last_course_ids = public.get("lastCourseIds") or []
    last_search_query = public.get("lastSearchQuery")
    return f"""
You are a pre-execution guard for the CINX learning assistant.
Decide whether the agent has enough information to call the tool immediately.

Intent: {intent}

Relevant session state:
  activeProposalId:      {public.get("activeProposalId")}
  activeProposalVersion: {public.get("activeProposalVersion")}
  awaitingConfirmation:  {public.get("awaitingConfirmation")}
  lastCourseIds:         {last_course_ids}
  lastSearchQuery:       {last_search_query}

Recent conversation:
{json.dumps(memory[-4:], ensure_ascii=False)}

Latest user message:
{message}

<rules>
COURSE_SEARCH:
  actionable=false if message contains no topic, keyword, category, or filter.

LEARNING_PATH_PROPOSAL:
  actionable=false if no goal, topic, or skill is stated AND both lastCourseIds and lastSearchQuery are empty.
  References to the previous keyword, search result, or course are actionable when lastSearchQuery or lastCourseIds exists.

LEARNING_PATH_EDIT:
  actionable=false if activeProposalId is null.
  actionable=false if no structural edit operation is described in the message.
  Structural edits are adding/removing a course, adding/removing a lesson, or moving/reordering a lesson.
  Generic requests that only ask to edit the path without saying what to change are not actionable.

LEARNING_PATH_COMMIT:
  actionable=false if activeProposalId is null.
  If awaitingConfirmation=true: treat the message as explicit confirmation only when it
  contains a clear affirmative signal (yes, ok, sure, go ahead, confirm, etc. in any language).
  Ambiguous or negative signals -> actionable=false.

All other intents: actionable=true.
</rules>

<language_rule>
{_LANG_RULE}
Write the "question" field in the same language as the latest user message.
</language_rule>

Return strict JSON only:
{{
  "actionable": true,
  "missingFields": [],
  "question": null,
  "reason": "short reason in English"
}}
"""


def confirmation_prompt(
    message: str,
    proposal_id: str,
    memory: list[dict[str, str]],
) -> str:
    return f"""
You are a confirmation verifier for the CINX learning assistant.
The user was asked to confirm creating learning path proposal: {proposal_id}

Latest user message:
{message}

Recent conversation:
{json.dumps(memory[-3:], ensure_ascii=False)}

<confirmed_signals>
Explicit affirmatives in any language:
yes / ok / sure / go ahead / confirm / proceed / do it / sounds good /
and their equivalents in other languages
</confirmed_signals>

<not_confirmed_signals>
Treat as confirmed=false:
- Negative: no / cancel / stop / wait / never mind and equivalents
- Questions or counter-proposals: "what if...", "can you change...", "actually..."
- Ambiguous fillers: "maybe", "hmm", "I don't know"
- Silence or topic change
Default: when in doubt, confirmed=false.
</not_confirmed_signals>

Return strict JSON only:
{{
  "confirmed": false,
  "reason": "short reason in English"
}}
"""


def agent_instructions(
    intent: AgentIntent,
    decision: AgentRouteDecision | None = None,
    state: dict[str, Any] | None = None,
) -> str:
    allowed_tools = ", ".join(decision.allowedTools) if decision else "the provided tools"
    flow = decision.nextFlow if decision else ""
    flow_instructions = _flow_final_instructions(flow)
    return f"""
You are the CINX learning assistant.

Intent: {intent}
Flow: {flow}
Allowed tools: {allowed_tools}
Session state: {json.dumps(public_state_context(state or {}), ensure_ascii=False)}

<constraints>
- Use only the allowed tool for this flow. Do not call any other tool.
- After tool output is available, answer using only that output.
  Do not invent course IDs, lesson IDs, policy facts, prices, ratings, or proposal IDs.
- For learning_path_create: use activeProposalId and activeProposalVersion from state
  only when the latest user message explicitly confirms.
- Never expose tool names, function call syntax, XML tags, or raw JSON to the user
  unless the flow contract explicitly requires JSON output.
- If a required detail is missing, say so clearly without inventing data.
</constraints>

<language_rule>
{_LANG_RULE}
</language_rule>

{flow_instructions}
"""


def _flow_final_instructions(flow: str) -> str:
    if flow == "course_search_flow":
        return """
<flow_rules>
- Use course_search to search for courses by queries or filters.
- Use course_get_details to retrieve the full curriculum/lessons of a specific course using its courseId.
- When describing a course's lessons or curriculum, rely on the output of course_get_details.
- If no results are found, tell the user clearly.
- Keep the response concise and user-facing.
</flow_rules>
"""
    if flow == "learning_path_edit_flow":
        return """
<flow_rules>
- Only make structural edits (add/remove courses, add/remove lessons, or reorder lessons) or explicit metadata edits (rename, update description).
- To add courses or lessons by name/topic, you should first search for them using course_search (to find course IDs) and/or call course_get_details (to list lesson IDs).
- Once you have the exact IDs, call learning_path_update_proposal with the target operation.
- Use batch parameters (courseIds / lessonIds) to add or remove multiple items in a single tool call.
- Do not change the title or description of the proposal unless the user explicitly asks to rename it or update its description.
- Confirm the update using only data from the returned proposal.
- Mention the current proposal version when available.
- Summarize the updated learning path at a high level. Do not print raw JSON.
</flow_rules>
"""
    if flow == "learning_path_proposal_flow":
        return """
<output_contract>
When the latest user message refers to prior search/session context or requests a new learning path:
1. If the user refers to previously searched/discussed courses (present in session state `lastCourseIds` or `lastCourseSummaries`), you can skip calling course_search and directly call course_get_details using those course IDs.
2. Otherwise, first search for matching courses using course_search.
3. For each relevant course, call course_get_details to retrieve its curriculum and lesson IDs.
4. Construct the learning path proposal using ONLY the valid courseId and lessonId fields obtained from the tool outputs.

Return ONLY valid JSON - no preamble, no explanation outside the JSON block.
If you cannot produce a valid proposal, return:
{"answer": "Unable to generate a proposal at this time.", "proposal": null}

Required shape:
{
  "answer": "<2-5 sentence user-facing summary in the user's language>",
  "proposal": {
    "title": "<string>",
    "description": "<string>",
    "items": [
      {
        "courseId": "<exact ID from tool output>",
        "lessonId": "<exact ID from tool output>",
        "orderIndex": 0,
        "isSuggested": true
      }
    ]
  }
}

Validation: every courseId and lessonId MUST appear verbatim in the tool output of course_get_details.
</output_contract>
"""
    if flow == "learning_path_commit_flow":
        return """
<flow_rules>
- If the tool output confirms success (success=true or a created path ID is present):
  confirm creation and include the ID.
- If the tool output indicates failure (success=false or an error field):
  explain the issue briefly and suggest the user try again.
- Never claim the path was created if the tool output does not confirm it.
</flow_rules>
"""
    if flow == "policy_qa_flow":
        return """
<flow_rules>
- Answer using only the retrieved policy contexts from the tool output.
- If the contexts do not answer the question, say the available policy data
  is insufficient to answer.
</flow_rules>
"""
    return ""


def direct_answer_prompt(message: str) -> str:
    return f"""
You are the CINX learning assistant. Answer the user without calling any tools.

User message:
{message}

<constraints>
- Keep the answer focused on learning, course discovery, study planning, or how CINX can help.
- Do not invent course IDs, prices, certificates, or policy facts.
- If catalog or policy data is needed, let the user know you can look it up if they ask.
</constraints>

<language_rule>
{_LANG_RULE}
</language_rule>
"""


def repair_prompt(
    message: str,
    intent: AgentIntent,
    answer: str,
    combined_output: dict[str, Any],
    grounding_error: str | None,
) -> str:
    return f"""
Repair the answer so it is grounded in the tool outputs only.

Intent: {intent}
User message: {message}
Proposal answer:
{answer}
Grounding error:
{grounding_error or ""}
Tool outputs:
{json.dumps(combined_output, ensure_ascii=False)}

<language_rule>
{_LANG_RULE}
</language_rule>

Rules:
- Return only a user-facing answer in the same language as the user message.
- Never return XML, DSML, tool_calls, function_call, invoke tags, function arguments, or raw JSON unless the flow explicitly requires JSON proposal output.
- Keep IDs exactly as provided in tool outputs.
"""

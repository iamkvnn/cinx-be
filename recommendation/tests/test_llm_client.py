import os
import sys
import unittest
from pathlib import Path
from unittest.mock import Mock, patch


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

os.environ.setdefault("DB_HOST", "localhost")
os.environ.setdefault("DB_PORT", "3306")
os.environ.setdefault("DB_NAME", "recommendationdb")
os.environ.setdefault("DB_USER", "root")
os.environ.setdefault("DB_PASSWORD", "root")
os.environ.setdefault("RABBITMQ_HOST", "localhost")
os.environ.setdefault("RABBITMQ_PORT", "5672")
os.environ.setdefault("RABBITMQ_USER", "guest")
os.environ.setdefault("RABBITMQ_PASSWORD", "guest")
os.environ.setdefault("DIGITALOCEAN_MODEL_ACCESS_KEY", "")
os.environ.setdefault("DIGITALOCEAN_INFERENCE_BASE_URL", "https://inference.do-ai.run/v1")
os.environ.setdefault("DIGITALOCEAN_LLM_MODEL", "llama3.3-70b-instruct")
os.environ.setdefault("DIGITALOCEAN_LLM_TIMEOUT_SECONDS", "30")


class DigitalOceanLLMClientTest(unittest.TestCase):
    def test_missing_key_returns_configuration_error(self):
        from app.core.config import settings
        from app.services.llm_client import DigitalOceanLLMClient

        settings.DIGITALOCEAN_MODEL_ACCESS_KEY = ""
        client = DigitalOceanLLMClient()

        text, error = client.generate_learning_path_json("prompt")

        self.assertIsNone(text)
        self.assertIn("DigitalOcean model access key not configured", error)

    def test_success_returns_message_content(self):
        from app.core.config import settings
        from app.services.llm_client import DigitalOceanLLMClient

        settings.DIGITALOCEAN_MODEL_ACCESS_KEY = "test-key"
        response = Mock()
        response.choices = [
            Mock(message=Mock(content='{"pathName":"A","items":[]}'))
        ]
        completions = Mock()
        completions.create.return_value = response
        client_api = Mock()
        client_api.chat.completions = completions

        client = DigitalOceanLLMClient()
        client._client = client_api
        text, error = client.generate_learning_path_json("prompt")

        self.assertIsNone(error)
        self.assertEqual(text, '{"pathName":"A","items":[]}')
        self.assertEqual(completions.create.call_args.kwargs["model"], settings.DIGITALOCEAN_LLM_MODEL)
        self.assertEqual(
            completions.create.call_args.kwargs["messages"][1],
            {"role": "user", "content": "prompt"},
        )


class JsonExtractionTest(unittest.TestCase):
    def test_extracts_json_from_markdown_fence(self):
        from app.services.rag_service import RAGService

        service = RAGService(db=Mock())
        text = '```json\n{"pathName":"A","items":[]}\n```'

        self.assertEqual(service._extract_json(text), '{"pathName":"A","items":[]}')

    def test_extracts_json_from_extra_text(self):
        from app.services.rag_service import RAGService

        service = RAGService(db=Mock())
        text = 'Here is the result: {"pathName":"A","items":[]} thanks'

        self.assertEqual(service._extract_json(text), '{"pathName":"A","items":[]}')


if __name__ == "__main__":
    unittest.main()

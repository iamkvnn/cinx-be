package com.cinx.user.service.user;

import com.cinx.user.consts.Gender;
import com.cinx.user.consts.PartnershipTerminationReasonType;
import com.cinx.user.consts.Role;
import com.cinx.user.consts.UserStatus;
import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UserDto;
import com.cinx.user.dto.request.TerminatePartnershipRequest;
import com.cinx.user.mapper.UserMapper;
import com.cinx.user.messaging.UserEventProducer;
import com.cinx.user.model.User;
import com.cinx.user.repository.UserDeviceTokenRepository;
import com.cinx.user.repository.UserRepository;
import com.cinx.user.service.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDeviceTokenRepository userDeviceTokenRepository;
    @Mock
    private UserEventProducer userEventProducer;
    @Mock
    private UserMapper userMapper;
    @Mock
    private S3Service s3Service;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "s3CdnUrl", "https://cdn.example.com");
    }

    @Test
    void createUserPersistsProfileFields() {
        CreateUserRequest request = new CreateUserRequest(
                "user-1",
                "Tran Thi B",
                "tranthib@example.com",
                Role.USER,
                Gender.FEMALE,
                "0987654321",
                "Bio",
                "cv.pdf"
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(dto());

        userService.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPhoneNumber()).isEqualTo("0987654321");
        assertThat(saved.getBio()).isEqualTo("Bio");
        assertThat(saved.getCvUrl()).isEqualTo("https://cdn.example.com/cv.pdf");
        verify(userRepository, never()).findAllByRole(Role.ADMIN);
    }

    @Test
    void createInstructorNotifiesAllAdmins() {
        CreateUserRequest request = new CreateUserRequest(
                "instructor-1",
                "Instructor",
                "instructor@example.com",
                Role.INSTRUCTOR,
                Gender.MALE,
                null,
                null,
                null
        );
        User admin1 = User.builder().userId("admin-1").role(Role.ADMIN).build();
        User admin2 = User.builder().userId("admin-2").role(Role.ADMIN).build();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(List.of(admin1, admin2));
        when(userMapper.toDto(any(User.class))).thenReturn(dto());

        userService.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userEventProducer).sendNewInstructorNotification(captor.capture(), org.mockito.ArgumentMatchers.eq(List.of("admin-1", "admin-2")));
        assertThat(captor.getValue().getUserId()).isEqualTo("instructor-1");
    }

    @Test
    void verifyInstructorStoresVerificationTime() {
        User user = User.builder()
                .userId("user-1")
                .email("instructor@example.com")
                .name("Instructor")
                .build();
        when(userRepository.findByUserId("user-1")).thenReturn(Optional.of(user));

        userService.verifyInstructor("user-1");

        assertThat(user.getIsInstructorVerified()).isTrue();
        assertThat(user.getInstructorVerifiedAt()).isNotNull();
        verify(userRepository).save(user);
        verify(userEventProducer).sendInstructorVerifiedEmail(user);
    }

    @Test
    void updateLastAccessStoresCurrentTimestamp() {
        User user = User.builder().userId("user-1").build();
        when(userRepository.findByUserId("user-1")).thenReturn(Optional.of(user));

        userService.updateLastAccess("user-1");

        assertThat(user.getLastAccessAt()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void terminatePartnershipStoresTerminationReasonAndTime() {
        User user = User.builder()
                .userId("instructor-1")
                .role(Role.INSTRUCTOR)
                .isInstructorVerified(true)
                .isPartnershipTerminated(false)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByUserId("instructor-1")).thenReturn(Optional.of(user));

        userService.terminatePartnership("instructor-1", new TerminatePartnershipRequest(
                PartnershipTerminationReasonType.POLICY_VIOLATION,
                " Repeated violations "
        ));

        assertThat(user.getIsInstructorVerified()).isFalse();
        assertThat(user.getIsPartnershipTerminated()).isTrue();
        assertThat(user.getPartnershipTerminatedAt()).isNotNull();
        assertThat(user.getPartnershipTerminationReasonType()).isEqualTo(PartnershipTerminationReasonType.POLICY_VIOLATION);
        assertThat(user.getPartnershipTerminationReasonDetail()).isEqualTo("Repeated violations");
        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
        verify(userRepository).save(user);
        verify(userEventProducer).sendPartnershipTerminatedEmail(user);
    }

    @Test
    void terminatePartnershipAllowsBlankReasonDetail() {
        User user = User.builder()
                .userId("instructor-1")
                .role(Role.INSTRUCTOR)
                .build();
        when(userRepository.findByUserId("instructor-1")).thenReturn(Optional.of(user));

        userService.terminatePartnership("instructor-1", new TerminatePartnershipRequest(
                PartnershipTerminationReasonType.ADMIN_DECISION,
                " "
        ));

        assertThat(user.getPartnershipTerminationReasonType()).isEqualTo(PartnershipTerminationReasonType.ADMIN_DECISION);
        assertThat(user.getPartnershipTerminationReasonDetail()).isNull();
        verify(userRepository).save(user);
        verify(userEventProducer).sendPartnershipTerminatedEmail(user);
    }

    @Test
    void terminatePartnershipRejectsNonInstructor() {
        User user = User.builder()
                .userId("user-1")
                .role(Role.USER)
                .build();
        when(userRepository.findByUserId("user-1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.terminatePartnership("user-1", new TerminatePartnershipRequest(
                PartnershipTerminationReasonType.OTHER,
                null
        )))
                .hasMessage("User is not an instructor");

        verify(userRepository, never()).save(any(User.class));
        verify(userEventProducer, never()).sendPartnershipTerminatedEmail(any(User.class));
    }

    private UserDto dto() {
        return new UserDto(
                "user-1",
                "Tran Thi B",
                "tranthib@example.com",
                Role.USER,
                Gender.FEMALE,
                false,
                false,
                null,
                null,
                "0987654321",
                "Bio",
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }
}

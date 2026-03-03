package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.question.*;
import com.atstudio.atstudio.entity.Answer;
import com.atstudio.atstudio.entity.Question;
import com.atstudio.atstudio.entity.QuestionAttachment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.*;
import com.atstudio.atstudio.repository.AnswerRepository;
import com.atstudio.atstudio.repository.QuestionAttachmentRepository;
import com.atstudio.atstudio.repository.QuestionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionService 단위 테스트")
class QuestionServiceTest {

    @Mock QuestionRepository questionRepository;
    @Mock AnswerRepository answerRepository;
    @Mock QuestionAttachmentRepository attachmentRepository;
    @Mock UserRepository userRepository;
    @Mock StorageService storageService;

    @InjectMocks QuestionService questionService;

    // ── 8.1 createQuestion ──────────────────────────────────────────────────

    @Nested
    @DisplayName("createQuestion()")
    class CreateQuestion {

        @Test
        @DisplayName("성공 - 첨부파일 없이 문의 생성")
        void success_noAttachments() {
            User user = buildUser(1L, UserRole.USER);
            Question saved = buildQuestion(1L, user, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(questionRepository.save(any(Question.class))).willReturn(saved);

            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setTitle("제목");
            request.setContent("내용");
            request.setCategory(QuestionCategory.DOWNLOAD);
            request.setIsPublic(false);

            QuestionResponse result = questionService.createQuestion(request, buildUserDetails(1L, UserRole.USER));

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.title()).isEqualTo("제목");
            assertThat(result.status()).isEqualTo("OPEN");
            assertThat(result.attachments()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 첨부파일 포함 문의 생성")
        void success_withAttachments() {
            User user = buildUser(1L, UserRole.USER);
            Question saved = buildQuestion(1L, user, "제목", "내용",
                    QuestionCategory.PAYMENT, true, QuestionStatus.OPEN);
            QuestionAttachment attachment = buildAttachment(1L, saved, "screenshot.png",
                    "questions/attachments/abc.png", 204800L);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(questionRepository.save(any(Question.class))).willReturn(saved);
            given(storageService.store(any(MultipartFile.class), eq("questions/attachments")))
                    .willReturn("questions/attachments/abc.png");
            given(attachmentRepository.save(any(QuestionAttachment.class))).willReturn(attachment);

            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setTitle("제목");
            request.setContent("내용");
            request.setCategory(QuestionCategory.PAYMENT);
            request.setIsPublic(true);
            request.setAttachments(List.of(
                    new MockMultipartFile("attachments", "screenshot.png",
                            "image/png", new byte[]{1, 2, 3})
            ));

            QuestionResponse result = questionService.createQuestion(request, buildUserDetails(1L, UserRole.USER));

            assertThat(result.attachments()).hasSize(1);
            assertThat(result.attachments().get(0).originalName()).isEqualTo("screenshot.png");
            verify(storageService).store(any(MultipartFile.class), eq("questions/attachments"));
        }
    }

    // ── 8.2 createAnswer ────────────────────────────────────────────────────

    @Nested
    @DisplayName("createAnswer()")
    class CreateAnswer {

        @Test
        @DisplayName("성공 - 소유자가 답변 작성")
        void success_owner() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);
            Answer saved = buildAnswer(1L, question, owner, "답변 내용");

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));
            given(userRepository.findById(1L)).willReturn(Optional.of(owner));
            given(answerRepository.save(any(Answer.class))).willReturn(saved);

            AnswerResponse result = questionService.createAnswer(10L,
                    new AnswerCreateRequest("답변 내용"), buildUserDetails(1L, UserRole.USER));

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.content()).isEqualTo("답변 내용");
        }

        @Test
        @DisplayName("성공 - ADMIN 첫 답변 시 OPEN → IN_PROGRESS 자동 전환")
        void success_adminFirstAnswer_statusTransition() {
            User owner = buildUser(1L, UserRole.USER);
            User admin = buildUser(99L, UserRole.ADMIN);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);
            Answer saved = buildAnswer(1L, question, admin, "관리자 답변");

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));
            given(userRepository.findById(99L)).willReturn(Optional.of(admin));
            given(answerRepository.save(any(Answer.class))).willReturn(saved);

            questionService.createAnswer(10L,
                    new AnswerCreateRequest("관리자 답변"), buildUserDetails(99L, UserRole.ADMIN));

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("성공 - ADMIN 답변이지만 이미 IN_PROGRESS → 상태 변경 없음")
        void success_adminAnswer_alreadyInProgress() {
            User owner = buildUser(1L, UserRole.USER);
            User admin = buildUser(99L, UserRole.ADMIN);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.IN_PROGRESS);
            Answer saved = buildAnswer(2L, question, admin, "추가 답변");

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));
            given(userRepository.findById(99L)).willReturn(Optional.of(admin));
            given(answerRepository.save(any(Answer.class))).willReturn(saved);

            questionService.createAnswer(10L,
                    new AnswerCreateRequest("추가 답변"), buildUserDetails(99L, UserRole.ADMIN));

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("실패 - 소유자도 ADMIN도 아닌 사용자 → RESOURCE_NOT_ACCESS")
        void fail_notOwnerNotAdmin() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> questionService.createAnswer(10L,
                    new AnswerCreateRequest("무단 답변"), buildUserDetails(50L, UserRole.USER)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
        }
    }

    // ── 8.3 getQuestions ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getQuestions()")
    class GetQuestions {

        @Test
        @DisplayName("성공 - 일반 유저 조회 (페이지네이션)")
        void success_normalUser() {
            Page<Question> page = new PageImpl<>(List.of());
            given(questionRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                    any(org.springframework.data.domain.Pageable.class))).willReturn(page);

            ResponseDTO<QuestionListItemResponse> result =
                    questionService.getQuestions(1, 20, null, null, null,
                            buildUserDetails(1L, UserRole.USER));

            assertThat(result).isNotNull();
            assertThat(result.getDataList()).isEmpty();
        }

        @Test
        @DisplayName("성공 - ADMIN 전체 조회")
        void success_admin() {
            Page<Question> page = new PageImpl<>(List.of());
            given(questionRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                    any(org.springframework.data.domain.Pageable.class))).willReturn(page);

            ResponseDTO<QuestionListItemResponse> result =
                    questionService.getQuestions(1, 20, null, null, null,
                            buildUserDetails(99L, UserRole.ADMIN));

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("성공 - category/status 필터")
        void success_withFilters() {
            Page<Question> page = new PageImpl<>(List.of());
            given(questionRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                    any(org.springframework.data.domain.Pageable.class))).willReturn(page);

            ResponseDTO<QuestionListItemResponse> result =
                    questionService.getQuestions(1, 20, QuestionCategory.DOWNLOAD,
                            QuestionStatus.OPEN, true, buildUserDetails(1L, UserRole.USER));

            assertThat(result).isNotNull();
        }
    }

    // ── 8.4 getQuestion ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getQuestion()")
    class GetQuestion {

        @Test
        @DisplayName("성공 - 공개 문의 조회")
        void success_publicQuestion() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, true, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));
            given(attachmentRepository.findAllByQuestionId(10L)).willReturn(List.of());
            given(answerRepository.findAllByQuestionOrderByCreatedAtAsc(question)).willReturn(List.of());

            QuestionResponse result = questionService.getQuestion(10L,
                    buildUserDetails(50L, UserRole.USER));

            assertThat(result.id()).isEqualTo(10L);
            assertThat(result.isPublic()).isTrue();
        }

        @Test
        @DisplayName("성공 - 비공개 문의를 소유자가 조회")
        void success_privateQuestion_owner() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));
            given(attachmentRepository.findAllByQuestionId(10L)).willReturn(List.of());
            given(answerRepository.findAllByQuestionOrderByCreatedAtAsc(question)).willReturn(List.of());

            QuestionResponse result = questionService.getQuestion(10L,
                    buildUserDetails(1L, UserRole.USER));

            assertThat(result.id()).isEqualTo(10L);
        }

        @Test
        @DisplayName("성공 - 비공개 문의를 ADMIN이 조회")
        void success_privateQuestion_admin() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));
            given(attachmentRepository.findAllByQuestionId(10L)).willReturn(List.of());
            given(answerRepository.findAllByQuestionOrderByCreatedAtAsc(question)).willReturn(List.of());

            QuestionResponse result = questionService.getQuestion(10L,
                    buildUserDetails(99L, UserRole.ADMIN));

            assertThat(result.id()).isEqualTo(10L);
        }

        @Test
        @DisplayName("실패 - 비공개 문의 + 타인 → RESOURCE_NOT_ACCESS")
        void fail_privateQuestion_otherUser() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> questionService.getQuestion(10L,
                    buildUserDetails(50L, UserRole.USER)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문의 → RESOURCE_NOT_FOUND")
        void fail_notFound() {
            given(questionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.getQuestion(99L,
                    buildUserDetails(1L, UserRole.USER)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }
    }

    // ── 8.5 downloadAttachment ──────────────────────────────────────────────

    @Nested
    @DisplayName("downloadAttachment()")
    class DownloadAttachment {

        @Test
        @DisplayName("성공 - 공개 문의 첨부파일 다운로드")
        void success() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, true, QuestionStatus.OPEN);
            QuestionAttachment attachment = buildAttachment(5L, question,
                    "file.png", "questions/attachments/file.png", 1024L);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));
            given(attachmentRepository.findByIdAndQuestionId(5L, 10L))
                    .willReturn(Optional.of(attachment));
            Resource mockResource = new ByteArrayResource(new byte[]{1, 2, 3});
            given(storageService.loadAsResource("questions/attachments/file.png"))
                    .willReturn(mockResource);

            Resource result = questionService.downloadAttachment(10L, 5L,
                    buildUserDetails(50L, UserRole.USER));

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("실패 - 비공개 문의 + 타인 → RESOURCE_NOT_ACCESS")
        void fail_privateQuestion_otherUser() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> questionService.downloadAttachment(10L, 5L,
                    buildUserDetails(50L, UserRole.USER)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
        }

        @Test
        @DisplayName("실패 - 첨부파일 미존재 → RESOURCE_NOT_FOUND")
        void fail_attachmentNotFound() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, true, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));
            given(attachmentRepository.findByIdAndQuestionId(99L, 10L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.downloadAttachment(10L, 99L,
                    buildUserDetails(1L, UserRole.USER)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }
    }

    // ── 8.6 updateQuestionStatus ────────────────────────────────────────────

    @Nested
    @DisplayName("updateQuestionStatus()")
    class UpdateQuestionStatus {

        @Test
        @DisplayName("성공 - OPEN → IN_PROGRESS")
        void success() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));

            QuestionResponse result = questionService.updateQuestionStatus(10L,
                    new QuestionStatusUpdateRequest(QuestionStatus.IN_PROGRESS));

            assertThat(result.status()).isEqualTo("IN_PROGRESS");
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문의 → RESOURCE_NOT_FOUND")
        void fail_notFound() {
            given(questionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.updateQuestionStatus(99L,
                    new QuestionStatusUpdateRequest(QuestionStatus.CLOSED)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }
    }

    // ── 8.7 deleteQuestion ──────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteQuestion()")
    class DeleteQuestion {

        @Test
        @DisplayName("성공 - 소유자 + OPEN 상태 삭제")
        void success_ownerOpenStatus() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));

            questionService.deleteQuestion(10L, buildUserDetails(1L, UserRole.USER));

            verify(attachmentRepository).deleteAllByQuestion(question);
            verify(answerRepository).deleteAllByQuestion(question);
            verify(questionRepository).delete(question);
        }

        @Test
        @DisplayName("성공 - ADMIN은 어떤 상태든 삭제 가능")
        void success_admin() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.IN_PROGRESS);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));

            questionService.deleteQuestion(10L, buildUserDetails(99L, UserRole.ADMIN));

            verify(attachmentRepository).deleteAllByQuestion(question);
            verify(answerRepository).deleteAllByQuestion(question);
            verify(questionRepository).delete(question);
        }

        @Test
        @DisplayName("성공 - Answer 포함 Question 삭제 시 자식 먼저 삭제 (CR-C-001)")
        void success_cascadeDeleteWithAnswers() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));

            questionService.deleteQuestion(10L, buildUserDetails(1L, UserRole.USER));

            var inOrder = inOrder(attachmentRepository, answerRepository, questionRepository);
            inOrder.verify(attachmentRepository).deleteAllByQuestion(question);
            inOrder.verify(answerRepository).deleteAllByQuestion(question);
            inOrder.verify(questionRepository).delete(question);
        }

        @Test
        @DisplayName("성공 - Attachment 포함 Question 삭제 시 자식 먼저 삭제 (CR-C-001)")
        void success_cascadeDeleteWithAttachments() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.PAYMENT, true, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));

            questionService.deleteQuestion(10L, buildUserDetails(1L, UserRole.USER));

            verify(attachmentRepository).deleteAllByQuestion(question);
            verify(answerRepository).deleteAllByQuestion(question);
            verify(questionRepository).delete(question);
        }

        @Test
        @DisplayName("실패 - 소유자 + IN_PROGRESS 상태 → RESOURCE_NOT_ACCESS")
        void fail_ownerNonOpenStatus() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.IN_PROGRESS);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> questionService.deleteQuestion(10L,
                    buildUserDetails(1L, UserRole.USER)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
        }

        @Test
        @DisplayName("실패 - 타인 삭제 시도 → RESOURCE_NOT_ACCESS")
        void fail_notOwner() {
            User owner = buildUser(1L, UserRole.USER);
            Question question = buildQuestion(10L, owner, "제목", "내용",
                    QuestionCategory.DOWNLOAD, false, QuestionStatus.OPEN);

            given(questionRepository.findById(10L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> questionService.deleteQuestion(10L,
                    buildUserDetails(50L, UserRole.USER)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문의 → RESOURCE_NOT_FOUND")
        void fail_notFound() {
            given(questionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.deleteQuestion(99L,
                    buildUserDetails(1L, UserRole.USER)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private User buildUser(Long id, UserRole role) {
        User user = User.builder()
                .email("user" + id + "@test.com").nickname("user" + id).password("pw")
                .userType(UserType.INDIVIDUAL).role(role).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Question buildQuestion(Long id, User user, String title, String content,
                                    QuestionCategory category, boolean isPublic,
                                    QuestionStatus status) {
        Question question = Question.builder()
                .user(user).title(title).content(content)
                .category(category).isPublic(isPublic).status(status).build();
        ReflectionTestUtils.setField(question, "id", id);
        return question;
    }

    private Answer buildAnswer(Long id, Question question, User user, String content) {
        Answer answer = Answer.builder()
                .question(question).user(user).content(content).build();
        ReflectionTestUtils.setField(answer, "id", id);
        return answer;
    }

    private QuestionAttachment buildAttachment(Long id, Question question,
                                                String originalName, String filePath,
                                                Long fileSize) {
        QuestionAttachment attachment = QuestionAttachment.builder()
                .question(question).originalName(originalName)
                .filePath(filePath).fileSize(fileSize).build();
        ReflectionTestUtils.setField(attachment, "id", id);
        return attachment;
    }

    private CustomUserDetails buildUserDetails(Long id, UserRole role) {
        return CustomUserDetails.builder()
                .id(id).email("user" + id + "@test.com").password("pw")
                .role(role).isDeleted(false).isProfileComplete(true)
                .build();
    }
}

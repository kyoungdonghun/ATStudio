package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.question.*;
import com.atstudio.atstudio.entity.Answer;
import com.atstudio.atstudio.entity.Question;
import com.atstudio.atstudio.entity.QuestionAttachment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.QuestionCategory;
import com.atstudio.atstudio.entity.enums.QuestionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.AnswerRepository;
import com.atstudio.atstudio.repository.QuestionAttachmentRepository;
import com.atstudio.atstudio.repository.QuestionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.spec.QuestionSpecification;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    // ── 8.1 POST /api/questions ─────────────────────────────────────────────

    @Transactional
    public QuestionResponse createQuestion(QuestionCreateRequest request,
                                            CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        Question question = Question.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .isPublic(request.getIsPublic())
                .build();
        question = questionRepository.save(question);

        List<QuestionAttachment> attachments = saveAttachments(question, request.getAttachments());

        return QuestionResponse.fromCreate(question, attachments);
    }

    // ── 8.2 POST /api/questions/{questionId}/answers ────────────────────────

    @Transactional
    public AnswerResponse createAnswer(Long questionId,
                                        AnswerCreateRequest request,
                                        CustomUserDetails userDetails) {
        Question question = findQuestionById(questionId);

        // Only the question owner or ADMIN can answer
        boolean isOwner = question.getUser().getId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        Answer answer = Answer.builder()
                .question(question)
                .user(user)
                .content(request.content())
                .build();
        answer = answerRepository.save(answer);

        // Auto-transition: ADMIN's first answer → OPEN → IN_PROGRESS
        if (isAdmin && question.getStatus() == QuestionStatus.OPEN) {
            question.updateStatus(QuestionStatus.IN_PROGRESS);
        }

        return AnswerResponse.from(answer);
    }

    // ── 8.3 GET /api/questions ──────────────────────────────────────────────

    public ResponseDTO<QuestionListItemResponse> getQuestions(int page, int size,
                                                              QuestionCategory category,
                                                              QuestionStatus status,
                                                              Boolean mine,
                                                              CustomUserDetails userDetails) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean isAdmin = userDetails.getRole() == UserRole.ADMIN;

        Specification<Question> spec = Specification.where(
                (root, query, cb) -> cb.conjunction());
        spec = addSpec(spec, QuestionSpecification.visibleTo(userDetails.getId(), isAdmin));
        spec = addSpec(spec, QuestionSpecification.hasCategory(category));
        spec = addSpec(spec, QuestionSpecification.hasStatus(status));
        if (Boolean.TRUE.equals(mine)) {
            spec = addSpec(spec, QuestionSpecification.belongsToUser(userDetails.getId()));
        }

        Page<Question> result = questionRepository.findAll(spec, pageable);

        List<QuestionListItemResponse> dataList = result.getContent().stream()
                .map(QuestionListItemResponse::from)
                .toList();
        int total = (int) result.getTotalElements();

        return ResponseDTO.<QuestionListItemResponse>builder()
                .dataList(dataList)
                .pageInfo(PageInfo.of(page, size, total, 10))
                .build();
    }

    // ── 8.4 GET /api/questions/{questionId} ─────────────────────────────────

    public QuestionResponse getQuestion(Long questionId, CustomUserDetails userDetails) {
        Question question = findQuestionById(questionId);
        checkReadAccess(question, userDetails);

        List<QuestionAttachment> attachments = attachmentRepository.findAllByQuestionId(questionId);
        List<Answer> answers = answerRepository.findAllByQuestionOrderByCreatedAtAsc(question);

        return QuestionResponse.from(question, attachments, answers);
    }

    // ── 8.5 GET /api/questions/{questionId}/attachments/{attachmentId} ──────

    public Resource downloadAttachment(Long questionId, Long attachmentId,
                                        CustomUserDetails userDetails) {
        Question question = findQuestionById(questionId);
        checkReadAccess(question, userDetails);

        QuestionAttachment attachment = attachmentRepository.findByIdAndQuestionId(attachmentId, questionId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        return storageService.loadAsResource(attachment.getFilePath());
    }

    // ── 8.6 PUT /api/questions/{questionId}/status ──────────────────────────

    @Transactional
    public QuestionResponse updateQuestionStatus(Long questionId,
                                                  QuestionStatusUpdateRequest request) {
        Question question = findQuestionById(questionId);
        question.updateStatus(request.status());
        return QuestionResponse.fromStatusUpdate(question);
    }

    // ── 8.7 DELETE /api/questions/{questionId} ──────────────────────────────

    @Transactional
    public void deleteQuestion(Long questionId, CustomUserDetails userDetails) {
        Question question = findQuestionById(questionId);

        boolean isOwner = question.getUser().getId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getRole() == UserRole.ADMIN;

        // Owner can only delete OPEN questions; ADMIN can delete any
        if (!isAdmin && !(isOwner && question.getStatus() == QuestionStatus.OPEN)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }

        // Delete children first to avoid FK constraint violation (CR-C-001)
        attachmentRepository.deleteAllByQuestion(question);
        answerRepository.deleteAllByQuestion(question);
        questionRepository.delete(question);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Question findQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private void checkReadAccess(Question question, CustomUserDetails userDetails) {
        if (question.isPublic()) return;
        boolean isOwner = question.getUser().getId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
    }

    private List<QuestionAttachment> saveAttachments(Question question,
                                                      List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();

        List<QuestionAttachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String filePath = storageService.store(file, "questions/attachments");
            QuestionAttachment attachment = QuestionAttachment.builder()
                    .question(question)
                    .originalName(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .build();
            attachments.add(attachmentRepository.save(attachment));
        }
        return attachments;
    }

    private Specification<Question> addSpec(Specification<Question> base,
                                             Specification<Question> other) {
        return other != null ? base.and(other) : base;
    }
}

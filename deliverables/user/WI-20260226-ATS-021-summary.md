# WI-20260226-ATS-021 Summary

## Inquiry Domain Implementation (APIs 8.1-8.7)

### Implemented APIs (7 endpoints)
| API | Method | Path | Description |
|-----|--------|------|-------------|
| 8.1 | POST | /api/questions | Create question (multipart with attachments) |
| 8.2 | POST | /api/questions/{id}/answers | Create answer (owner/ADMIN only) |
| 8.3 | GET | /api/questions | List questions (filtered, paginated) |
| 8.4 | GET | /api/questions/{id} | Question detail with answers & attachments |
| 8.5 | GET | /api/questions/{id}/attachments/{id} | Download attachment file |
| 8.6 | PUT | /api/questions/{id}/status | Update status (ADMIN only) |
| 8.7 | DELETE | /api/questions/{id} | Delete question (owner+OPEN or ADMIN) |

### Created Files
| File | Type |
|------|------|
| `dto/question/QuestionCreateRequest.java` | Request DTO |
| `dto/question/AnswerCreateRequest.java` | Request DTO |
| `dto/question/QuestionStatusUpdateRequest.java` | Request DTO |
| `dto/question/QuestionResponse.java` | Response DTO |
| `dto/question/QuestionListItemResponse.java` | Response DTO |
| `dto/question/AnswerResponse.java` | Response DTO |
| `dto/question/AttachmentResponse.java` | Response DTO |
| `repository/spec/QuestionSpecification.java` | Specification |
| `service/QuestionService.java` | Service |
| `controller/QuestionController.java` | Controller |
| `service/QuestionServiceTest.java` | Service Test (24 cases) |
| `controller/QuestionControllerTest.java` | Controller Test (15 cases) |

### Modified Files
| File | Change |
|------|--------|
| `entity/Question.java` | Added `updateStatus()` method |
| `repository/QuestionRepository.java` | Added `JpaSpecificationExecutor` |
| `repository/AnswerRepository.java` | Added query methods |
| `repository/QuestionAttachmentRepository.java` | Added query methods |

### Key Design Decisions
1. Access control enforced at service layer (not @PreAuthorize) for owner-or-ADMIN logic
2. ADMIN-first-answer auto-transitions OPEN -> IN_PROGRESS
3. `Specification.where(cb.conjunction())` base used to avoid Spring Data 4.x null Specification issue
4. File attachments stored via StorageService to `questions/attachments` directory
5. SecurityConfig already had `/api/questions/*/status` ADMIN rule -- no changes needed

### Remaining Risks
- None identified. All 362 tests pass (323 existing + 39 new), 0 failures.

package com.atstudio.atstudio.dto.question;

import com.atstudio.atstudio.entity.enums.QuestionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.atstudio.atstudio.common.validation.ValidationConstants.*;

@Getter
@Setter
@NoArgsConstructor
public class QuestionCreateRequest {

    @NotBlank
    @Size(max = TITLE_QUESTION_MAX)
    private String title;

    @NotBlank
    @Size(max = DESCRIPTION_MAX)
    private String content;

    @NotNull
    private QuestionCategory category;

    @NotNull
    private Boolean isPublic;

    private List<MultipartFile> attachments;
}

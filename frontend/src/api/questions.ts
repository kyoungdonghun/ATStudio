import client from './client';
import { getBinaryDownload, type BinaryDownload } from './downloads';
import type { ApiResponse, PageInfo, QuestionCategory, QuestionStatus } from '@/types';

/* ── Types ── */

export interface QuestionListItem {
  id: number;
  title: string;
  category: QuestionCategory;
  isPublic: boolean;
  status: QuestionStatus;
  createdAt: string;
}

export interface AttachmentInfo {
  id: number;
  originalName: string;
  fileSize: number;
}

export interface AnswerInfo {
  id: number;
  content: string;
  user: { id: number; nickname: string; role: string };
  createdAt: string;
}

export interface QuestionDetail {
  id: number;
  title: string;
  content: string;
  category: QuestionCategory;
  isPublic: boolean;
  status: QuestionStatus;
  user: { id: number; nickname: string } | null;
  attachments: AttachmentInfo[] | null;
  answers: AnswerInfo[] | null;
  createdAt: string;
}

export interface QuestionListResponse {
  dataList: QuestionListItem[];
  pageInfo: PageInfo;
}

export interface QuestionStatusUpdateResponse {
  id: number;
  title: string;
  category: QuestionCategory;
  isPublic: boolean;
  status: QuestionStatus;
  createdAt: string;
}

/* ── API Functions ── */

/** 8.3 GET /api/questions — list (mine=true for subscriber) */
export async function fetchQuestions(
  params: {
    page?: number;
    size?: number;
    category?: QuestionCategory;
    status?: QuestionStatus;
    mine?: boolean;
  },
  signal?: AbortSignal,
): Promise<QuestionListResponse> {
  const { data } = await client.get<QuestionListResponse>('/questions', { params, signal });
  return data;
}

/** 8.4 GET /api/questions/{id} — detail */
export async function fetchQuestionDetail(
  id: number,
  signal?: AbortSignal,
): Promise<QuestionDetail> {
  const { data } = await client.get<ApiResponse<QuestionDetail>>(`/questions/${id}`, { signal });
  return data.data;
}

/** 8.1 POST /api/questions — create (multipart) */
export async function createQuestion(body: {
  title: string;
  content: string;
  category: string;
  isPublic: boolean;
  attachments?: File[];
}): Promise<QuestionDetail> {
  const form = new FormData();
  form.append('title', body.title);
  form.append('content', body.content);
  form.append('category', body.category);
  form.append('isPublic', String(body.isPublic));
  if (body.attachments) {
    body.attachments.forEach((file) => form.append('attachments', file));
  }
  const { data } = await client.post<ApiResponse<QuestionDetail>>('/questions', form, {
    timeout: 60_000, // 1 minute for attachment upload
  });
  return data.data;
}

/** 8.7 DELETE /api/questions/{id} */
export async function deleteQuestion(id: number): Promise<void> {
  await client.delete(`/questions/${id}`);
}

/** 8.6 PUT /api/questions/{id}/status (admin) */
export async function updateQuestionStatus(
  id: number,
  status: QuestionStatus,
): Promise<QuestionStatusUpdateResponse> {
  const { data } = await client.put<ApiResponse<QuestionStatusUpdateResponse>>(
    `/questions/${id}/status`,
    { status },
  );
  return data.data;
}

/** 8.2 POST /api/questions/{id}/answers — create answer */
export async function createAnswer(questionId: number, content: string): Promise<AnswerInfo> {
  const { data } = await client.post<ApiResponse<AnswerInfo>>(`/questions/${questionId}/answers`, {
    content,
  });
  return data.data;
}

/** 8.5 Download attachment (blob with JWT auth) */
export async function downloadAttachment(
  questionId: number,
  attachmentId: number,
  fallbackFileName: string,
  signal?: AbortSignal,
): Promise<BinaryDownload> {
  return getBinaryDownload(
    `/questions/${questionId}/attachments/${attachmentId}`,
    fallbackFileName,
    {
      signal,
    },
  );
}

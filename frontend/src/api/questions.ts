import client from './client';
import type { PageInfo } from '@/types';

/* ── Types ── */

export interface QuestionListItem {
  id: number;
  title: string;
  category: string;
  isPublic: boolean;
  status: string;
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
  category: string;
  isPublic: boolean;
  status: string;
  user: { id: number; nickname: string } | null;
  attachments: AttachmentInfo[] | null;
  answers: AnswerInfo[] | null;
  createdAt: string;
}

export interface QuestionListResponse {
  dataList: QuestionListItem[];
  pageInfo: PageInfo;
}

/* ── API Functions ── */

/** 8.3 GET /api/questions — list (mine=true for subscriber) */
export async function fetchQuestions(params: {
  page?: number;
  size?: number;
  category?: string;
  status?: string;
  mine?: boolean;
}): Promise<QuestionListResponse> {
  const { data } = await client.get('/questions', { params });
  return data;
}

/** 8.4 GET /api/questions/{id} — detail */
export async function fetchQuestionDetail(id: number): Promise<QuestionDetail> {
  const { data } = await client.get(`/questions/${id}`);
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
  const { data } = await client.post('/questions', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
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
  status: string,
): Promise<QuestionDetail> {
  const { data } = await client.put(`/questions/${id}/status`, { status });
  return data.data;
}

/** 8.5 GET attachment download URL */
export function getAttachmentUrl(questionId: number, attachmentId: number): string {
  return `/api/questions/${questionId}/attachments/${attachmentId}`;
}

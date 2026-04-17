import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ForumComment {
  id?: number | string;
  postId: number | string;
  author: string;
  content: string;
  createdAt?: string;
}

export interface ForumPost {
  id?: number | string;
  title: string;
  content: string;
  author: string;
  category?: string;
  createdAt?: string;
  comments?: ForumComment[];
  commentCount?: number;
  likes?: number;
}

@Injectable({ providedIn: 'root' })
export class ForumService {
  private readonly api = 'http://localhost:8083/api/forum';

  constructor(private http: HttpClient) {}

  // Posts CRUD
  getPosts(): Observable<ForumPost[]>                                    { return this.http.get<ForumPost[]>(`${this.api}/posts`); }
  createPost(p: ForumPost): Observable<ForumPost>                        { return this.http.post<ForumPost>(`${this.api}/posts`, p); }
  updatePost(id: number|string, p: Partial<ForumPost>): Observable<ForumPost> { return this.http.put<ForumPost>(`${this.api}/posts/${id}`, p); }
  deletePost(id: number|string): Observable<void>                        { return this.http.delete<void>(`${this.api}/posts/${id}`); }

  // Comments CRUD
  getComments(postId: number|string): Observable<ForumComment[]>                                           { return this.http.get<ForumComment[]>(`${this.api}/posts/${postId}/comments`); }
  createComment(postId: number|string, c: ForumComment): Observable<ForumComment>                          { return this.http.post<ForumComment>(`${this.api}/posts/${postId}/comments`, c); }
  deleteComment(postId: number|string, commentId: number|string): Observable<void>                         { return this.http.delete<void>(`${this.api}/posts/${postId}/comments/${commentId}`); }
}

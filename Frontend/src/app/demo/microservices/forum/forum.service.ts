import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface ForumComment {
  id?: number | string;
  postId?: number | string;
  author: string;
  content: string;
  createdAt?: string | Date;
}

export interface ForumPost {
  id?: number | string;
  title: string;
  content: string;
  author: string;
  category?: string;
  categoryId?: number;
  createdAt?: string | Date;
  likes?: number;
  commentCount?: number;
  comments?: ForumComment[];
}

@Injectable({ providedIn: 'root' })
export class ForumService {
  private readonly api = 'http://localhost:8083/api/forum';

  constructor(private http: HttpClient) {}

  private getHeaders() {
    const token = localStorage.getItem('token');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return { headers };
  }

  private getJsonHeaders() {
    return {
      headers: this.getHeaders().headers
        .set('Content-Type', 'application/json')
        .set('Accept', 'application/json')
    };
  }

  private mapPost(p: any): ForumPost {
    return {
      id: p.id, title: p.title, content: p.content,
      author: p.authorUsername || p.author || 'Anonymous',
      category: p.categoryName || p.category || 'General',
      categoryId: p.categoryId || 1,
      createdAt: p.createdAt, likes: p.likeCount || p.likes || 0,
      commentCount: p.commentCount || 0
    };
  }

  private mapComment(c: any): ForumComment {
    return {
      id: c.id, postId: c.postId, content: c.content,
      author: c.authorUsername || c.author || 'Anonymous',
      createdAt: c.createdAt
    };
  }

  // Posts CRUD
  getPosts(): Observable<ForumPost[]> { 
    return this.http.get<any[]>(`${this.api}/posts`, this.getHeaders())
      .pipe(map(posts => posts.map(p => this.mapPost(p)))); 
  }
  
  createPost(p: ForumPost): Observable<ForumPost> {
    const payload = { title: p.title, content: p.content, categoryId: 1 };
    return this.http.post<any>(`${this.api}/posts`, payload, this.getJsonHeaders())
      .pipe(map(res => this.mapPost(res)));
  }
  
  updatePost(id: number|string, p: Partial<ForumPost>): Observable<ForumPost> { 
    const payload = { title: p.title, content: p.content, categoryId: 1 };
    return this.http.put<any>(`${this.api}/posts/${id}`, payload, this.getJsonHeaders())
      .pipe(map(res => this.mapPost(res)));
  }
  
  deletePost(id: number|string): Observable<void> { 
    return this.http.delete<void>(`${this.api}/posts/${id}`, this.getHeaders()); 
  }

  // Comments CRUD
  getComments(postId: number|string): Observable<ForumComment[]> { 
    return this.http.get<any[]>(`${this.api}/posts/${postId}/comments`, this.getHeaders())
      .pipe(map(comments => comments.map(c => this.mapComment(c)))); 
  }
  
  createComment(postId: number|string, c: ForumComment): Observable<ForumComment> { 
    return this.http.post<any>(`${this.api}/posts/${postId}/comments`, { content: c.content }, this.getJsonHeaders())
      .pipe(map(res => this.mapComment(res)));
  }
  
  deleteComment(postId: number|string, commentId: number|string): Observable<void> { 
    return this.http.delete<void>(`${this.api}/posts/${postId}/comments/${commentId}`, this.getHeaders()); 
  }
}

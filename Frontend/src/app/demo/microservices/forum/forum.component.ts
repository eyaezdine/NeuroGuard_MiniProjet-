import {
  Component, OnInit, OnDestroy,
  ChangeDetectorRef, ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ForumService, ForumPost, ForumComment } from './forum.service';

type SortKey = 'newest' | 'oldest' | 'mostLiked' | 'mostComments';

@Component({
  selector: 'app-forum',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './forum.component.html',
  styleUrls: ['./forum.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ForumComponent implements OnInit, OnDestroy {

  /* ── Data ──────────────────────────────────────────────────────────── */
  allPosts: ForumPost[]      = [];
  filteredPosts: ForumPost[] = [];
  readonly categories = ['General', 'Medical', 'Wellness', 'Support', 'Updates'];

  /* ── UI state ────────────────────────────────────────────────────────*/
  loading    = false;
  submitting = false;
  error      = '';
  toast      = '';
  toastKind: 'success' | 'error' = 'success';

  /* ── Search / sort / filter ───────────────────────────────────────────*/
  searchQuery = '';
  sortBy: SortKey = 'newest';
  categoryFilter = 'All';

  /* ── Inline expanded state ───────────────────────────────────────────*/
  expandedPostId: string | null = null;
  editingPostId:  string | null = null;
  showNewPostForm = false;

  /* ── Forms ───────────────────────────────────────────────────────────*/
  newPostForm!: FormGroup;
  editForm!: FormGroup;
  commentForms: Record<string, FormGroup>  = {};

  /* ── Likes (local) ───────────────────────────────────────────────────*/
  likedIds = new Set<string>();
  likeCounts: Record<string, number> = {};

  /* ── Delete confirm ──────────────────────────────────────────────────*/
  pendingDeleteId: string | null = null;

  /* ── Stats ───────────────────────────────────────────────────────────*/
  get totalPosts()    { return this.allPosts.length; }
  get totalComments() { return this.allPosts.reduce((s,p)=>s+(p.comments?.length??p.commentCount??0),0); }
  get totalLikes()    { return Object.values(this.likeCounts).reduce((s,v)=>s+v,0); }
  get uniqueAuthors() { return new Set(this.allPosts.map(p=>p.author)).size; }

  private destroy$ = new Subject<void>();

  constructor(
    private svc: ForumService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  /* ── Lifecycle ────────────────────────────────────────────────────── */
  ngOnInit() {
    this.newPostForm = this.fb.group({
      title:    ['', [Validators.required, Validators.minLength(5), Validators.maxLength(120)]],
      content:  ['', [Validators.required, Validators.minLength(10)]],
      category: ['General', Validators.required]
    });
    this.editForm = this.fb.group({
      title:    ['', [Validators.required, Validators.minLength(5), Validators.maxLength(120)]],
      content:  ['', [Validators.required, Validators.minLength(10)]],
      category: ['General', Validators.required]
    });
    this.loadPosts();
  }

  ngOnDestroy() { this.destroy$.next(); this.destroy$.complete(); }

  /* ── Load ─────────────────────────────────────────────────────────── */
  loadPosts() {
    this.loading = true; this.error = ''; this.cdr.markForCheck();
    this.svc.getPosts().pipe(takeUntil(this.destroy$)).subscribe({
      next: posts => {
        this.allPosts = posts;
        posts.forEach(p => {
          const key = String(p.id);
          if (!(key in this.likeCounts)) this.likeCounts[key] = p.likes ?? 0;
        });
        this.filter(); this.loading = false; this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Cannot reach forum-service. Backend might be offline.';
        this.loading = false; this.cdr.markForCheck();
      }
    });
  }

  /* ── Filter / sort ────────────────────────────────────────────────── */
  filter() {
    let res = this.allPosts;
    
    // 1. Category filter
    if (this.categoryFilter !== 'All') {
      res = res.filter(p => p.category === this.categoryFilter);
    }

    // 2. Text Search filter
    const q = this.searchQuery.toLowerCase().trim();
    if (q) {
      res = res.filter(p =>
        p.title.toLowerCase().includes(q) ||
        p.content.toLowerCase().includes(q) ||
        p.author.toLowerCase().includes(q) ||
        (p.category || '').toLowerCase().includes(q)
      );
    }

    // 3. Sort
    switch (this.sortBy) {
      case 'newest':
        res.sort((a,b) => new Date(b.createdAt??0).getTime() - new Date(a.createdAt??0).getTime()); break;
      case 'oldest':
        res.sort((a,b) => new Date(a.createdAt??0).getTime() - new Date(b.createdAt??0).getTime()); break;
      case 'mostLiked':
        res.sort((a,b) => (this.likeCounts[String(b.id)]??0) - (this.likeCounts[String(a.id)]??0)); break;
      case 'mostComments':
        res.sort((a,b) => this.commentCount(b) - this.commentCount(a)); break;
    }
    
    this.filteredPosts = res; this.cdr.markForCheck();
  }

  clearSearch() { this.searchQuery = ''; this.filter(); }

  /* ── Create post ──────────────────────────────────────────────────── */
  toggleNewPostForm() {
    this.showNewPostForm = !this.showNewPostForm;
    if (!this.showNewPostForm) this.newPostForm.reset({ category: 'General' });
    this.cdr.markForCheck();
  }

  submitNewPost() {
    if (this.newPostForm.invalid || this.submitting) return;
    this.submitting = true; this.cdr.markForCheck();
    const post: ForumPost = {
      title:    this.newPostForm.value.title.trim(),
      content:  this.newPostForm.value.content.trim(),
      category: this.newPostForm.value.category,
      author:   this.me()
    };
    this.svc.createPost(post).pipe(takeUntil(this.destroy$)).subscribe({
      next: p => {
        this.allPosts = [p, ...this.allPosts];
        this.likeCounts[String(p.id)] = p.likes ?? 0;
        this.newPostForm.reset({ category: 'General' });
        this.showNewPostForm = false;
        this.submitting = false;
        this.filter();
        this.notify('Post published! 🎉');
      },
      error: () => { this.submitting = false; this.notify('Failed to publish – backend offline?', 'error'); }
    });
  }

  /* ── Edit post (inline) ───────────────────────────────────────────── */
  startEdit(post: ForumPost) {
    this.editingPostId = String(post.id);
    this.editForm.setValue({
      title: post.title,
      content: post.content,
      category: post.category || 'General'
    });
    this.cdr.markForCheck();
  }

  cancelEdit() { this.editingPostId = null; this.cdr.markForCheck(); }

  submitEdit(post: ForumPost) {
    if (this.editForm.invalid || !post.id || this.submitting) return;
    this.submitting = true; this.cdr.markForCheck();
    this.svc.updatePost(post.id, {
      title:    this.editForm.value.title.trim(),
      content:  this.editForm.value.content.trim(),
      category: this.editForm.value.category
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: updated => {
        this.allPosts = this.allPosts.map(p => p.id === updated.id ? {...p,...updated} : p);
        this.editingPostId = null; this.submitting = false;
        this.filter(); this.notify('Post updated ✓');
      },
      error: () => { this.submitting = false; this.notify('Failed to update post.', 'error'); }
    });
  }

  /* ── Delete post ──────────────────────────────────────────────────── */
  askDelete(postId: string) { this.pendingDeleteId = postId; this.cdr.markForCheck(); }
  cancelDeleteConfirm()     { this.pendingDeleteId = null;   this.cdr.markForCheck(); }

  confirmDelete() {
    if (!this.pendingDeleteId || this.submitting) return;
    const id = this.pendingDeleteId;
    this.submitting = true; this.cdr.markForCheck();
    this.svc.deletePost(id).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.allPosts = this.allPosts.filter(p => String(p.id) !== id);
        delete this.likeCounts[id];
        this.pendingDeleteId = null; this.submitting = false;
        if (this.expandedPostId === id) this.expandedPostId = null;
        this.filter(); this.notify('Post deleted.');
      },
      error: () => { this.submitting = false; this.notify('Failed to delete.', 'error'); }
    });
  }

  /* ── Comments ─────────────────────────────────────────────────────── */
  toggleComments(post: ForumPost) {
    const key = String(post.id);
    if (this.expandedPostId === key) { this.expandedPostId = null; this.cdr.markForCheck(); return; }
    this.expandedPostId = key;
    if (!post.comments) this.fetchComments(post);
    this.cdr.markForCheck();
  }

  private fetchComments(post: ForumPost) {
    this.svc.getComments(post.id!).pipe(takeUntil(this.destroy$)).subscribe({
      next: comments => {
        this.allPosts = this.allPosts.map(p => p.id === post.id ? {...p, comments} : p);
        this.filter();
      },
      error: () => {}
    });
  }

  commentFormFor(postId: string | number): FormGroup {
    const k = String(postId);
    if (!this.commentForms[k]) {
      this.commentForms[k] = this.fb.group({ content: ['', [Validators.required, Validators.minLength(2)]] });
    }
    return this.commentForms[k];
  }

  submitComment(post: ForumPost) {
    if (!post.id) return;
    const form = this.commentFormFor(post.id);
    if (form.invalid) return;
    const c: ForumComment = { postId: post.id, content: form.value.content.trim(), author: this.me() };
    this.svc.createComment(post.id, c).pipe(takeUntil(this.destroy$)).subscribe({
      next: saved => {
        this.allPosts = this.allPosts.map(p =>
          p.id === post.id ? {...p, comments: [...(p.comments??[]), saved]} : p
        );
        form.reset(); this.filter();
      },
      error: () => this.notify('Failed to add comment.', 'error')
    });
  }

  deleteComment(post: ForumPost, comment: ForumComment) {
    if (!post.id || !comment.id) return;
    this.svc.deleteComment(post.id, comment.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.allPosts = this.allPosts.map(p =>
          p.id === post.id ? {...p, comments: (p.comments??[]).filter(c => c.id !== comment.id)} : p
        );
        this.filter();
      },
      error: () => this.notify('Failed to delete comment.', 'error')
    });
  }

  /* ── Like ─────────────────────────────────────────────────────────── */
  toggleLike(post: ForumPost) {
    const key = String(post.id);
    if (this.likedIds.has(key)) {
      this.likedIds.delete(key);
      this.likeCounts[key] = Math.max(0, (this.likeCounts[key] ?? 1) - 1);
    } else {
      this.likedIds.add(key);
      this.likeCounts[key] = (this.likeCounts[key] ?? 0) + 1;
    }
    if (this.sortBy === 'mostLiked') this.filter();
    else this.cdr.markForCheck();
  }

  isLiked(post: ForumPost): boolean { return this.likedIds.has(String(post.id)); }
  likeCount(post: ForumPost): number { return this.likeCounts[String(post.id)] ?? post.likes ?? 0; }

  /* ── Share ────────────────────────────────────────────────────────── */
  async sharePost(post: ForumPost) {
    const url  = `${window.location.origin}/ms/forum`;
    const text = `${post.title}\n\n${post.content.slice(0,120)}…`;
    if ('share' in navigator && navigator['share']) {
      try { await (navigator as any).share({ title: post.title, text, url }); return; } catch {}
    }
    try {
      await navigator.clipboard.writeText(`${text}\n\n${url}`);
      this.notify('Link copied to clipboard 📋');
    } catch { this.notify('Could not copy link.', 'error'); }
  }

  /* ── Helpers ──────────────────────────────────────────────────────── */
  me(): string {
    try { const u = JSON.parse(localStorage.getItem('user')??'{}'); return u.name||u.firstname||u.email||'Anonymous'; }
    catch { return 'Anonymous'; }
  }

  notify(msg: string, kind: 'success'|'error' = 'success') {
    this.toast = msg; this.toastKind = kind; this.error = kind === 'error' ? msg : '';
    this.cdr.markForCheck();
    setTimeout(() => { this.toast = ''; this.cdr.markForCheck(); }, 3500);
  }

  commentCount(p: ForumPost): number { return p.comments?.length ?? p.commentCount ?? 0; }

  initials(name: string): string {
    return name.split(' ').map(n=>n[0]).slice(0,2).join('').toUpperCase();
  }

  color(name: string): string {
    // Medical/Wellness dynamic palette
    const palette = ['#0ea5e9','#14b8a6','#10b981','#6366f1','#8b5cf6','#f59e0b','#ec4899','#f43f5e'];
    let h = 0; for (let i=0;i<name.length;i++) h = name.charCodeAt(i)+((h<<5)-h);
    return palette[Math.abs(h)%palette.length];
  }

  categoryColor(cat: string): string {
    switch (cat?.toLowerCase()) {
      case 'medical': return 'badge-medical';
      case 'wellness': return 'badge-wellness';
      case 'support': return 'badge-support';
      case 'updates': return 'badge-updates';
      default: return 'badge-general';
    }
  }

  trackId(_: number, item: any) { return item.id; }
  isExpanded(p: ForumPost) { return this.expandedPostId === String(p.id); }
  isEditing(p: ForumPost)  { return this.editingPostId  === String(p.id); }
}

export type MovieStatus = 'PENDING' | 'DOWNLOADING' | 'READY' | 'ERROR';

export interface MovieResponse {
  id: string;
  title: string;
  description?: string;
  year?: number;
  duration?: string;
  thumbnailUrl?: string;
  cached: boolean;
  favorite: boolean;
  status: MovieStatus;
  categoryId?: string;
  categoryName?: string;
  fileSize?: number;
  createdAt: string;
}

export interface MovieCreateRequest {
  title: string;
  description?: string;
  year?: number;
  duration?: string;
  megaUrl: string;
  thumbnailUrl?: string;
  categoryId?: string;
}

export interface MovieUpdateRequest {
  title: string;
  description?: string;
  year?: number;
  duration?: string;
  megaUrl: string;
  thumbnailUrl?: string;
  categoryId?: string;
}

export interface MovieGroupResponse {
  name: string;
  categoryId?: string;
  special: boolean;
  sortOrder: number;
  movies: MovieResponse[];
}

export interface CategoryResponse {
  id: string;
  name: string;
  description?: string;
  sortOrder?: number;
  movieCount: number;
}

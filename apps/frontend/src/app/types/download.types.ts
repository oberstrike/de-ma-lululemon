export type DownloadStatus = 'QUEUED' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';

export interface DownloadProgressResponse {
  movieId: string;
  movieTitle: string;
  status: DownloadStatus;
  bytesDownloaded: number;
  totalBytes: number;
  progress: number;
  errorMessage?: string;
}

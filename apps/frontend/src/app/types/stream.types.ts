export interface StreamInfoResponse {
  movieId: string;
  title: string;
  fileSize: number;
  contentType: string;
  streamUrl: string;
  supportsRangeRequests: boolean;
}

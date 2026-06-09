import { apiUrl, authHeaders } from "./apiConfig";

interface PresignedUrlResponse {
  presignedUrl: string;
  publicUrl: string;
}

export async function uploadFile(file: File): Promise<{ publicUrl: string; fileName: string }> {
  const params = new URLSearchParams({
    fileName: file.name,
    contentType: file.type || "application/octet-stream",
  });

  const response = await fetch(apiUrl(`/files/presigned-url?${params}`), {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("Could not get upload URL");
  }

  const { presignedUrl, publicUrl }: PresignedUrlResponse = await response.json();

  const uploadResponse = await fetch(presignedUrl, {
    method: "PUT",
    body: file,
    headers: { "Content-Type": file.type || "application/octet-stream" },
  });

  if (!uploadResponse.ok) {
    throw new Error("File upload to S3 failed");
  }

  return { publicUrl, fileName: file.name };
}

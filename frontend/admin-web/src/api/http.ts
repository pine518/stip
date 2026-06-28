import axios from 'axios'

export interface ApiResponse<T> {
  code: string
  message: string
  traceId: string
  data: T
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  timeout: 10000,
})

export async function getData<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const response = await http.get<ApiResponse<T>>(url, { params })
  return unwrap(response.data)
}

export async function postData<T>(url: string, body: unknown): Promise<T> {
  const response = await http.post<ApiResponse<T>>(url, body)
  return unwrap(response.data)
}

export async function patchData<T>(url: string, body: unknown): Promise<T> {
  const response = await http.patch<ApiResponse<T>>(url, body)
  return unwrap(response.data)
}

export async function deleteData<T>(url: string): Promise<T> {
  const response = await http.delete<ApiResponse<T>>(url)
  return unwrap(response.data)
}

function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== '0') {
    throw new Error(response.message || 'request failed')
  }
  return response.data
}

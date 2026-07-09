export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  nickname: string;
  password: string;
}

export interface LoginResponse {
  id: number;
  email: string;
  nickname: string;
  token: string;
}

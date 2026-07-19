export const ERROR_MESSAGES: Record<string, string> = {
  USER_NOT_FOUND: 'User not found',
  USER_ALREADY_EXISTS: 'An account with this email already exists',
  INVALID_CREDENTIALS: 'Invalid email or password',
  NOT_FOUND: 'Not found — it may have been deleted',
  FORBIDDEN: "You don't have permission to do this",
  BOARD_COLUMN_LIMIT_REACHED: 'Maximum of 5 columns per board',
  COLUMN_TASK_LIMIT_REACHED: 'Column is full (max 10 tasks)',
  SERVICE_UNAVAILABLE: 'Service temporarily unavailable — please try again',
  VALIDATION_FAILED: 'Please check the form and try again'
};

export function resolveErrorMessage(code: string | undefined, fallback: string): string {
  return (code && ERROR_MESSAGES[code]) || code || fallback;
}

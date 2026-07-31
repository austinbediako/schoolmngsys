import { ApiError } from '@/types';
import { UseFormSetError } from 'react-hook-form';
import { toast } from 'sonner';

export function handleApiError(error: any, setError?: UseFormSetError<any>) {
  if (error.status === 401) {
    window.location.href = '/login';
    return;
  }
  if (error.status === 403) {
    window.location.href = '/forbidden';
    return;
  }
  
  if (error.status === 409) {
    toast.error('Version Conflict', {
      description: 'This record was changed by someone else. Please reload and try again.',
      duration: 10000,
    });
    return;
  }

  const apiError = error as ApiError;
  
  if (apiError.type === 'rule-violation') {
    toast.error(`Rule Violation: ${apiError.title}`, {
      description: apiError.detail,
      duration: 8000,
    });
    return;
  }

  if (apiError.errors && apiError.errors.length > 0 && setError) {
    apiError.errors.forEach((err) => {
      setError(err.field, { message: err.message, type: 'server' });
    });
    toast.error('Validation Error', { description: 'Please check the form for errors.' });
    return;
  }

  toast.error(apiError.title || 'Error', {
    description: apiError.detail || 'An unexpected error occurred.',
  });
}

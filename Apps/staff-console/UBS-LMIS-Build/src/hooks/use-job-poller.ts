import { toast } from 'sonner';
import { apiClient } from '@/lib/api-client';

export function useJobPoller() {
  return async function pollJob(jobId: string, onComplete?: (result?: any) => void) {
    const toastId = toast.loading('Processing...', { duration: Infinity });
    
    const interval = setInterval(async () => {
      try {
        const job = await apiClient(`/jobs/${jobId}`);
        
        if (job.status === 'COMPLETED') {
          clearInterval(interval);
          toast.success(job.message || 'Operation completed', { id: toastId, duration: 3000 });
          if (onComplete) onComplete(job.result);
        } else if (job.status === 'FAILED') {
          clearInterval(interval);
          toast.error('Operation failed', { 
            id: toastId, 
            description: job.message || 'An error occurred during processing',
            duration: 8000
          });
        } else {
          // RUNNING or PENDING
          if (job.progress !== undefined) {
            toast.loading(`Processing... ${job.progress}%`, { id: toastId, duration: Infinity });
          }
        }
      } catch (err) {
        clearInterval(interval);
        toast.dismiss(toastId);
      }
    }, 2000);
  };
}

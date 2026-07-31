export function GhsCurrency({ amount, className = '' }: { amount: string | number, className?: string }) {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  const formatted = isNaN(num) ? '0.00' : num.toFixed(2);
  
  return (
    <span className={`tabular-nums ${className}`}>
      <span className="opacity-75 mr-0.5">GH₵</span>
      {formatted}
    </span>
  );
}

import * as React from 'react';
import { Slot } from '@radix-ui/react-slot';
import { cn } from '@/lib/utils';
import { cva, type VariantProps } from 'class-variance-authority';

const buttonVariants = cva(
  'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-xl text-xs font-semibold transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 disabled:pointer-events-none disabled:opacity-50 active:scale-98 [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0',
  {
    variants: {
      variant: {
        default:
          'bg-slate-900 hover:bg-slate-800 text-white shadow-xs border border-slate-900',
        secondary:
          'bg-white hover:bg-slate-50 text-slate-700 border border-slate-300 shadow-2xs',
        outline:
          'bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 shadow-2xs',
        destructive:
          'bg-white hover:bg-rose-50 text-rose-600 border border-rose-200 shadow-2xs',
        success:
          'bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs border border-emerald-600',
        ghost: 'bg-transparent text-slate-700 hover:bg-slate-100 border border-transparent',
        link: 'text-indigo-600 underline-offset-4 hover:underline p-0 h-auto font-bold',
      },
      size: {
        default: 'h-9 px-4 py-2',
        sm: 'h-8 px-3 text-[11px] rounded-lg',
        lg: 'h-10 px-6 text-sm rounded-xl',
        icon: 'h-9 w-9 p-0 rounded-xl',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
);

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : 'button';
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    );
  },
);
Button.displayName = 'Button';

export { Button, buttonVariants };

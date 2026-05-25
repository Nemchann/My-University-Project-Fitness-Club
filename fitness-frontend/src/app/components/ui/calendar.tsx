"use client";

import * as React from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { DayPicker } from "react-day-picker";

import { cn } from "./utils";
import { buttonVariants } from "./button";

// В 9-й версии тип пропсов берется напрямую из React.ComponentProps<typeof DayPicker>
export type CalendarProps = React.ComponentProps<typeof DayPicker>;

function Calendar({
  className,
  classNames,
  showOutsideDays = true,
  ...props
}: CalendarProps) {
  return (
    <DayPicker
      showOutsideDays={showOutsideDays}
      className={cn("p-3", className)}
      classNames={{
        months: "flex flex-col sm:flex-row gap-2",
        month: "flex flex-col gap-4",
        month_caption: "flex justify-center pt-1 relative items-center w-full mb-2",
        caption_label: "text-sm font-medium",
        nav: "flex items-center gap-1 absolute right-2 top-2 z-10",
        button_previous: cn(
          buttonVariants({ variant: "outline" }),
          "size-7 bg-transparent p-0 opacity-50 hover:opacity-100 cursor-pointer"
        ),
        button_next: cn(
          buttonVariants({ variant: "outline" }),
          "size-7 bg-transparent p-0 opacity-50 hover:opacity-100 cursor-pointer"
        ),
        month_grid: "w-full border-collapse space-y-1",
        weekdays: "flex",
        weekday: "text-muted-foreground rounded-md w-8 font-normal text-[0.8rem] text-center",
        week: "flex w-full mt-2",
        day: cn(
          buttonVariants({ variant: "ghost" }),
          "size-8 p-0 font-normal aria-selected:opacity-100 text-center rounded-md hover:bg-pink-100 transition-colors"
        ),
        // Стили для выбранного дня (твоя розовая тема)
        selected: "bg-pink-500 text-white hover:bg-pink-600 focus:bg-pink-500 focus:text-white rounded-md",
        today: "bg-accent text-accent-foreground font-bold",
        outside: "day-outside text-muted-foreground opacity-50",
        disabled: "text-muted-foreground opacity-50 line-through",
        hidden: "invisible",
        ...classNames,
      }}
      // В 9-й версии кастомные стрелочки передаются через один общий компонент Chevron
      components={{
        Chevron: ({ orientation, ...chevronProps }) => {
          return orientation === "left" ? (
            <ChevronLeft className="size-4" {...chevronProps} />
          ) : (
            <ChevronRight className="size-4" {...chevronProps} />
          );
        },
      }}
      {...props}
    />
  );
}

Calendar.displayName = "Calendar";

export { Calendar };
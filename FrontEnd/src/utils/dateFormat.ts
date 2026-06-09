const TIME_FORMATTER = new Intl.DateTimeFormat("en", {
  hour: "2-digit",
  minute: "2-digit",
});

const SHORT_DATE_FORMATTER = new Intl.DateTimeFormat("en", {
  month: "short",
  day: "numeric",
});

const FULL_DATE_FORMATTER = new Intl.DateTimeFormat("en", {
  month: "short",
  day: "numeric",
  year: "numeric",
});

function startOfLocalDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function isSameLocalDate(first: Date, second: Date): boolean {
  return (
    first.getFullYear() === second.getFullYear() &&
    first.getMonth() === second.getMonth() &&
    first.getDate() === second.getDate()
  );
}

function isYesterday(date: Date, now = new Date()): boolean {
  const today = startOfLocalDay(now);
  const candidate = startOfLocalDay(date);
  return today.getTime() - candidate.getTime() === 24 * 60 * 60 * 1000;
}

export function formatMessageTime(value: string): string {
  return TIME_FORMATTER.format(new Date(value));
}

export function formatMessageDateLabel(value: string): string {
  const date = new Date(value);
  const now = new Date();

  if (isSameLocalDate(date, now)) {
    return "Today";
  }

  if (isYesterday(date, now)) {
    return "Yesterday";
  }

  return date.getFullYear() === now.getFullYear()
    ? SHORT_DATE_FORMATTER.format(date)
    : FULL_DATE_FORMATTER.format(date);
}

export function shouldShowDateSeparator(currentValue: string, previousValue?: string): boolean {
  if (!previousValue) {
    return true;
  }

  return !isSameLocalDate(new Date(currentValue), new Date(previousValue));
}

export function formatChannelTimestamp(value: string): string {
  const date = new Date(value);
  const now = new Date();

  if (isSameLocalDate(date, now)) {
    return formatMessageTime(value);
  }

  if (isYesterday(date, now)) {
    return "Yesterday";
  }

  return date.getFullYear() === now.getFullYear()
    ? SHORT_DATE_FORMATTER.format(date)
    : FULL_DATE_FORMATTER.format(date);
}

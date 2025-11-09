//layout.js
import './globals.css';

export const metadata = {
  title: "Secure Keypad",
  description: "bob Secure Keypad",
};

export default function RootLayout({ children }) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}

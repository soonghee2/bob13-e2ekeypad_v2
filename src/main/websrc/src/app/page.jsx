// app/page.jsx
"use client";

import { motion } from "framer-motion";
import { Lock } from "lucide-react";
import useSecureKeypad from "../hooks/useSecureKeypad";
import SecureKeypad from "../components/SecureKeypad";
import KeypadUserInput from "../components/KeypadUserInput.jsx";

export default function Page() {
  const {
    keypad,
    keys,
    handleButtonClick,
    circleColors,
    status,
    error,
    message,
    refreshKeypad
  } = useSecureKeypad();

  const isReady = status === "ready" && Boolean(keypad);
  const isBusy = status === "loading" || status === "submitting";

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white rounded-3xl shadow-2xl shadow-slate-200 p-8"
        >
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-500 to-blue-600 rounded-2xl mb-4 shadow-lg shadow-blue-500/30">
              <Lock className="w-8 h-8 text-white" />
            </div>
            <h1 className="text-2xl font-semibold text-slate-800 mb-2">보안 비밀번호 입력</h1>
            <p className="text-sm text-slate-500">6자리 비밀번호를 입력해주세요</p>
          </div>

          <div className="flex justify-center gap-3 mb-12">
            {circleColors.map((color, index) => (
              <motion.span
                key={index}
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                transition={{ delay: index * 0.05, type: "spring", stiffness: 500, damping: 30 }}
                className="h-4 w-4 rounded-full"
                style={{ backgroundColor: color === "grey" ? "#e2e8f0" : color }}
              />
            ))}
          </div>

          <div className="e2ekaypad flex flex-col items-center">
            {isReady ? (
              <div className="w-full rounded-[32px] bg-gradient-to-br from-slate-50 via-white to-slate-100 p-4 shadow-[0_30px_80px_rgba(15,23,42,0.15)]">
                <div className="secure-keypad-container">
                  <SecureKeypad keypad={keypad} />
                  <KeypadUserInput
                    keys={keys}
                    onButtonClick={handleButtonClick}
                    disabled={!isReady}
                  />
                </div>
              </div>
            ) : (
              <div className="flex h-48 w-full items-center justify-center rounded-3xl border border-dashed border-slate-300 bg-slate-100 text-sm text-slate-500 text-center px-4">
                {status === "error" ? "키패드를 불러오는 중 오류가 발생했습니다. 다시 시도해주세요." : "키패드를 불러오는 중입니다..."}
              </div>
            )}
            {status === "submitting" && (
              <p className="mt-4 text-xs text-blue-600">서버에게 보내는 중...</p>
            )}
            {status === "error" && error && (
              <p className="mt-4 text-xs text-red-500">{error}</p>
            )}
            {message && status !== "error" && (
              <p className="mt-4 text-xs text-emerald-600">{message}</p>
            )}
            <button
              type="button"
              onClick={refreshKeypad}
              className="mt-4 text-xs text-blue-600 hover:text-blue-700 disabled:text-slate-400"
              disabled={isBusy}
            >
              새로운 키패드 받기
            </button>
          </div>

          <div className="mt-8 text-center">
            <button className="text-blue-600 hover:text-blue-700 transition-colors">비밀번호를 잊으셨나요?</button>
          </div>
        </motion.div>

        <div className="mt-6 text-center text-sm text-slate-500">
          <p>안전한 뱅킹을 위해 비밀번호를 타인에게 공유하지 마세요</p>
        </div>
      </div>
    </div>
  );
}

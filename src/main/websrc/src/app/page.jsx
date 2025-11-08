//app/page.jsx
//Next.js에서는 src/app/page.jsx가 라우트 엔트리로 규정됨. 
"use client";

import React from 'react';
import useSecureKeypad from '../hooks/useSecureKeypad';
import SecureKeypad from "../components/SecureKeypad";
import KeypadUserInput from "../components/KeypadUserInput.jsx";

export default function Page() {
    const { states, handleButtonClick, circleColors , isLoading} = useSecureKeypad();

    if (states.keypad === null) {
        return (
            <div>
                ...isLoading...
            </div>
        );
    } else if (isLoading){
        return (
            <div>
                서버에게 보내는 중
            </div>
        );
    } else {
        return (
            <div>
                <div className="circle-container">
                    {circleColors.map((color, index) => (
                        <div key={index} className="circle" style={{backgroundColor: color}}></div>
                    ))}

                </div>
                <div  className="secure-keypad-container">
                    <KeypadUserInput
                        userInput={states.userInput}
                        keys={states.keys}
                        onButtonClick={handleButtonClick}
                    />
                    <SecureKeypad
                        keypad={states.keypad}
                        // keys 배열을 SecureKeypad에 전달
                        // 클릭 핸들러를 전달
                    />
                </div>
            </div>
        );
    }
}

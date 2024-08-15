// // components/SecureKeypad.jsx
//
// import React from 'react';
// import '../style/keypad.css';
//
// export default function SecureKeypad({ keypad, keys, onButtonClick }) {
//     return (
//         <div className="secure-keypad-container">
//             <img src={keypad} alt="Keypad" className="secure-keypad-image" />
//             <div className="secure-keypad-grid">
//                 {keys.map((key, index) => (
//                     <button
//                         key={index}
//                         className="secure-keypad-button"
//                         onClick={() => onButtonClick(index)}  // 인덱스를 클릭 핸들러에 전달
//                         aria-label={`Button ${index + 1}`}  // 접근성을 위한 aria-label
//                     >
//                         {/* 텍스트를 숨깁니다. */}
//                     </button>
//                 ))}
//             </div>
//         </div>
//     );
// }

// components/SecureKeypad.jsx

import React from 'react';
import '../style/keypad.css';

export default function SecureKeypad({ keypad}) {

    return (
        // <div>

            <img src={keypad} alt="Keypad" className="secure-keypad-image"/>

        //</div>
    );
}

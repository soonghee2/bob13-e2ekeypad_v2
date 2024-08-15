//
//
// import '../style/keypad.css'
//
// export default function KeypadUserInput({ userInput }) {
//     return (
//         <>
//             <div className="input-group-style">
//                 {/*
//                 ???
//                 */}
//             </div>
//         </>
//     );
// }


import '../style/keypad.css'

export default function KeypadUserInput({ userInput, keys, onButtonClick }) {
    return (
            <div className="secure-keypad-grid">
                {keys.map((key, index) => (
                     <button
                         key={index}
                         className="secure-keypad-button"
                         onClick={() => onButtonClick(index)}  // 인덱스를 클릭 핸들러에 전달
                         aria-label={`Button ${index + 1}`}  // 접근성을 위한 aria-label
                     >
                         {/* 텍스트를 숨깁니다. */}
                     </button>
                 ))}
            </div>
    );
}

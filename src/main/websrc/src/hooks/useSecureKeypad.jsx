// // hooks/useSecureKeypad.js
// "use client";
//
// import { useState, useEffect, useCallback } from 'react';
//
// export default function useSecureKeypad() {
//     const [states, setStates] = useState({ keypad: null, userInput: '', keys: [] });
//     const [clickedHashes, setClickedHashes] = useState('');
//     const [clickCount, setClickCount] = useState(0);  // 클릭 횟수를 관리하는 상태
//
//     useEffect(() => {
//         const fetchImageAndKeys = async () => {
//             try {
//                 const response = await fetch('/api/combined-image');
//                 const data = await response.json();
//                 if (data && data.imageBase64) {
//                     setStates(prevStates => ({
//                         ...prevStates,
//                         keypad: `data:image/png;base64,${data.imageBase64}`,
//                         keys: data.keys || []  // 서버에서 keys를 받아와 상태에 저장
//                     }));
//                 }
//             } catch (error) {
//                 console.error('Failed to fetch image and keys:', error);
//             }
//         };
//
//         fetchImageAndKeys();
//     }, []);
//
//     const handleButtonClick = useCallback((index) => {
//         const key = states.keys[index] || '';
//         const cnt=0;
//         setClickedHashes(prevHashes => {
//             const newHashes = prevHashes + key;
//             console.log(`Button ${index + 1} pressed. Key: ${key}`);
//             console.log(`Concatenated hashes so far: ${newHashes}`);
//             console.log(newHashes.length);
//             setClickCount(prevCount => {
//                 const newCount = prevCount + 1;
//                 console.log(`Button pressed ${newCount} times`);
//
//                 if (newCount === 12) {  // 총 6번 클릭 시 (렌더링이 두번씩 되므로 일단 12번
//                     alert(`Concatenated hash: ${newHashes}`);
//                     window.location.reload();  // 페이지 새로고침
//                     return 0;  // 클릭 수를 초기화
//                 }
//                 return newCount;
//             });
//
//             return newHashes;
//
//         });
//     }, [states.keys]);
//
//     return { states, handleButtonClick };
// }
import { useState, useEffect, useCallback } from 'react';

export default function useSecureKeypad() {
    const [states, setStates] = useState({ keypad: null, userInput: '', keys: [] });
    const [clickedHashes, setClickedHashes] = useState('');
    const [clickCount, setClickCount] = useState(0);
    const [circleColors, setCircleColors] = useState(Array(6).fill('grey')); // 초기 색상은 회색


    useEffect(() => {
        const fetchImageAndKeys = async () => {
            try {
                const response = await fetch('/api/combined-image');
                const data = await response.json();
                if (data && data.imageBase64) {
                    setStates(prevStates => ({
                        ...prevStates,
                        keypad: `data:image/png;base64,${data.imageBase64}`,
                        keys: data.keys || []
                    }));
                }
            } catch (error) {
                console.error('Failed to fetch image and keys:', error);
            }
        };

        fetchImageAndKeys();
    }, []);

    const handleButtonClick = useCallback((index) => {
        const key = states.keys[index] || '';
        setClickedHashes(prevHashes => {
            const newHashes = prevHashes + key;
            console.log(`Button ${index + 1} pressed. Key: ${key}`);
            console.log(`Concatenated hashes so far: ${newHashes}`);
            console.log(newHashes.length);

            if (newHashes.length <= 240) { // 클릭 횟수가 6 이하일 때만 색상 변경
                setCircleColors(prevColors => {
                    const newColors = [...prevColors];
                    newColors[newHashes.length/40 - 1] = '#4eaeef';
                    return newColors;
                });
            }
            if (newHashes.length === 240) {
                alert(`Concatenated hash: ${newHashes}`);
                window.location.reload();
                return 0;
            }
            return newHashes;
        });
    }, [states.keys]);

    return { states, handleButtonClick ,  circleColors};
}

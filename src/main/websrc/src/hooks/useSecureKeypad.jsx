import { useState, useEffect, useCallback, useMemo } from 'react';
import { encryptWithPublicKey } from './incrypt';

const HASH_CHUNK_LENGTH = 40;
const REQUIRED_DIGITS = 6;
const REQUIRED_HASH_LENGTH = HASH_CHUNK_LENGTH * REQUIRED_DIGITS;

const INITIAL_STATE = {
    keypad: null,
    keys: [],
    uuid: '',
    hashedTimestamp: ''
};

export default function useSecureKeypad() {
    const [sessionState, setSessionState] = useState(INITIAL_STATE);
    const [publicKey, setPublicKey] = useState('');
    const [clickedHashes, setClickedHashes] = useState('');
    const [status, setStatus] = useState('idle'); // idle | loading | ready | submitting | error
    const [error, setError] = useState(null);
    const [message, setMessage] = useState('');

    const fetchKeypad = useCallback(async (signal) => {
        setStatus('loading');
        setError(null);
        try {
            const response = await fetch('/api/combined-image', { signal });
            if (!response.ok) {
                throw new Error('키패드를 불러오지 못했습니다.');
            }
            const data = await response.json();
            setSessionState({
                keypad: data?.imageBase64 ? `data:image/png;base64,${data.imageBase64}` : null,
                keys: data?.keys || [],
                uuid: data?.uuid || '',
                hashedTimestamp: data?.hashedTimestamp || ''
            });
            setClickedHashes('');
            setMessage('');
            setStatus('ready');
        } catch (fetchError) {
            if (fetchError.name === 'AbortError') {
                return;
            }
            console.error('Failed to fetch image and keys:', fetchError);
            setError(fetchError.message || '키패드를 불러오지 못했습니다.');
            setStatus('error');
        }
    }, []);

    useEffect(() => {
        const controller = new AbortController();
        fetchKeypad(controller.signal);
        return () => controller.abort();
    }, [fetchKeypad]);

    useEffect(() => {
        let isMounted = true;
        const fetchPublicKey = async () => {
            try {
                const response = await fetch('/public.pem');
                if (!response.ok) {
                    throw new Error('공개 키를 불러오지 못했습니다.');
                }
                const keyText = await response.text();
                if (isMounted) {
                    setPublicKey(keyText);
                }
            } catch (keyError) {
                console.error('Failed to load public key:', keyError);
                if (isMounted) {
                    setError('공개 키를 불러오지 못했습니다.');
                }
            }
        };

        fetchPublicKey();
        return () => {
            isMounted = false;
        };
    }, []);

    const submitData = useCallback(async (newHashes) => {
        if (!sessionState.uuid || !sessionState.hashedTimestamp) {
            setError('세션 정보가 올바르지 않습니다.');
            return;
        }
        if (!publicKey) {
            setError('공개 키를 아직 불러오는 중입니다.');
            return;
        }

        setStatus('submitting');
        setError(null);
        try {
            const encryptedHashes = encryptWithPublicKey(publicKey, newHashes);
            if (!encryptedHashes) {
                throw new Error('암호화에 실패했습니다.');
            }

            const response = await fetch('/api/submit-hashes', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    hashes: encryptedHashes,
                    uuid: sessionState.uuid,
                    hashedTimestamp: sessionState.hashedTimestamp
                })
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || '데이터를 전송하지 못했습니다.');
            }
            const responseText = await response.text();
            setMessage(responseText || '서버 응답을 받았습니다.');
            await fetchKeypad();
        } catch (submitError) {
            console.error('Error submitting data:', submitError);
            setError(submitError.message || '전송 도중 오류가 발생했습니다.');
            setStatus('ready');
        } finally {
            setClickedHashes('');
        }
    }, [publicKey, sessionState, fetchKeypad]);

    const handleButtonClick = useCallback((index) => {
        if (status !== 'ready') {
            return;
        }
        const key = sessionState.keys[index];
        if (!key) {
            return;
        }
        setClickedHashes((prevHashes) => {
            const nextHashes = prevHashes + key;
            if (nextHashes.length >= REQUIRED_HASH_LENGTH) {
                submitData(nextHashes);
                return '';
            }
            return nextHashes;
        });
    }, [sessionState.keys, status, submitData]);

    const circleColors = useMemo(() => {
        const filledSlots = Math.min(
            Math.floor(clickedHashes.length / HASH_CHUNK_LENGTH),
            REQUIRED_DIGITS
        );
        return Array.from({ length: REQUIRED_DIGITS }, (_, index) =>
            index < filledSlots ? '#4eaeef' : 'grey'
        );
    }, [clickedHashes]);

    const refreshKeypad = useCallback(() => {
        if (status === 'submitting') {
            return;
        }
        fetchKeypad();
    }, [fetchKeypad, status]);

    return {
        keypad: sessionState.keypad,
        keys: sessionState.keys,
        handleButtonClick,
        circleColors,
        status,
        error,
        message,
        refreshKeypad
    };
}

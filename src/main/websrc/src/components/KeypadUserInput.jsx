import '../style/keypad.css'

export default function KeypadUserInput({ keys, onButtonClick, disabled = false }) {
    return (
        <div className="secure-keypad-grid">
            {keys.map((_, index) => (
                <button
                    key={index}
                    type="button"
                    className="secure-keypad-button"
                    onClick={() => onButtonClick(index)}
                    aria-label={`Keypad button ${index + 1}`}
                    disabled={disabled}
                    aria-disabled={disabled}
                />
            ))}
        </div>
    );
}

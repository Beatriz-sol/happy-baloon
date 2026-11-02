# Gera os efeitos sonoros do jogo (WAV 16 bits mono, 44,1 kHz) por síntese simples.
import math, random, struct, sys, wave

RATE = 44100
OUT = sys.argv[1]

def tone(freq, dur, vol=0.5, shape="sine", decay=6.0, freq_end=None):
    n = int(RATE * dur)
    out = []
    phase = 0.0
    for i in range(n):
        t = i / RATE
        f = freq if freq_end is None else freq + (freq_end - freq) * (i / n)
        phase += 2 * math.pi * f / RATE
        if shape == "square":
            s = 1.0 if math.sin(phase) >= 0 else -1.0
            s *= 0.5
        elif shape == "noise":
            s = random.uniform(-1, 1) * 0.6 + math.sin(phase) * 0.4
        else:
            s = math.sin(phase) + 0.3 * math.sin(2 * phase)
        env = math.exp(-decay * t) * min(1.0, i / (RATE * 0.004)) * min(1.0, (n - i) / (RATE * 0.01))
        out.append(s * env * vol)
    return out

def silence(dur):
    return [0.0] * int(RATE * dur)

def save(name, samples):
    with wave.open(f"{OUT}/{name}", "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, s)) * 32000)) for s in samples))

random.seed(7)
save("moeda.wav", tone(988, 0.07, 0.35, "square", 2) + tone(1319, 0.22, 0.35, "square", 9))
save("passaro.wav", tone(420, 0.28, 0.6, "noise", 8, freq_end=120))
save("contagem.wav", tone(660, 0.14, 0.5, "sine", 12))
save("largada.wav", tone(990, 0.4, 0.55, "sine", 5))
save("fim.wav", tone(523, 0.12, 0.4, "square", 4) + tone(659, 0.12, 0.4, "square", 4)
     + tone(784, 0.12, 0.4, "square", 4) + tone(1047, 0.5, 0.4, "square", 4))
save("estrela.wav", tone(1568, 0.35, 0.4, "sine", 8) + silence(0.02))

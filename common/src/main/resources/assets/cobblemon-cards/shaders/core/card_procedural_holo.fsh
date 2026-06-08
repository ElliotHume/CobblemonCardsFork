#version 150

// ============================================================================
// Fragment Shader — Effets holo procéduraux pour CobblemonCards
// 20 effets portés depuis Java, exécutés entièrement sur GPU avec transparence.
// L'effectId est encodé dans vertexColor.r (0-19).
// Le temps vient du uniform GameTime de Minecraft.
// Le style pixel-art 40×30 est recréé via floor().
// ============================================================================

in vec4 vertexColor;
in vec2 texCoord0;

uniform float GameTime;

out vec4 fragColor;

// ===== UTILITAIRES =====

// Hash pseudo-aléatoire (pattern standard GPU)
float hash21(float x, float y) {
    return fract(abs(sin(x * 12.9898 + y * 78.233)) * 43758.5453);
}

float hash11(float n) {
    return fract(abs(sin(n)) * 43758.5453);
}

// ===== EFFETS INDIVIDUELS =====

// 0: holo_lines — Rayons lumineux diagonaux dynamiques
vec4 holo_lines(float u, float v, int ix, int iy, float time) {
    float oblique = u * 0.8 + v * 0.6;
    float wave = sin(oblique * 20.0 - time * 4.0)
               + sin(oblique * 35.0 - time * 6.0) * 0.5
               + sin(oblique * 5.0 + time * 2.0) * 0.5;
    if (wave > 1.2) {
        return vec4(0.9, 1.0, 1.0, 0.8);
    } else if (wave > 0.8) {
        float hue = mod(u + v - time, 3.0);
        if (hue < 0.0) hue += 3.0;
        vec3 color;
        if (hue < 1.0) color = vec3(0.4, 0.8, 1.0);
        else if (hue < 2.0) color = vec3(1.0, 0.4, 0.8);
        else color = vec3(1.0, 0.8, 0.4);
        return vec4(color, 0.5);
    } else if (wave > 0.4) {
        return vec4(0.2, 0.3, 0.6, 0.2);
    }
    return vec4(0.0);
}

// 1: holo_pulse — Cercle de lumière pulsante
vec4 holo_pulse(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = v - 0.5;
    float dist = sqrt(dx * dx + dy * dy);
    float wave = dist * 5.0 - time * 2.0;
    float pulse = fract(wave);
    if (pulse > 0.8) {
        return vec4(1.0, 1.0, 0.8, max(0.0, 0.5 * (1.0 - dist * 2.0)));
    }
    return vec4(0.0);
}

// 2: holo_rainbow — Effet arc-en-ciel diagonal
vec4 holo_rainbow(float u, float v, int ix, int iy, float time) {
    float hue = mod(u * 2.0 + v * 2.0 + time * 1.5, 3.0);
    vec3 color;
    if (hue < 1.0) {
        color = vec3(1.0, 0.2 + hue * 0.8, 0.2);
    } else if (hue < 2.0) {
        float h = hue - 1.0;
        color = vec3(1.0 - h * 0.8, 1.0, 0.2 + h * 0.8);
    } else {
        float h = hue - 2.0;
        color = vec3(0.2 + h * 0.8, 1.0 - h * 0.8, 1.0);
    }
    float sweep = sin((u - v) * 5.0 + time * 2.0);
    float a = (sweep > 0.5) ? 0.5 : 0.1;
    return vec4(color, a);
}

// 3: holo_sparkle — Éclats de confettis triangulaires / losanges
vec4 holo_sparkle(float u, float v, int ix, int iy, float time) {
    float distU = u + sin(v * 15.0) * 0.05;
    float distV = v + cos(u * 15.0) * 0.05;
    float cellX = floor(distU * 8.0);
    float cellY = floor(distV * 8.0);
    float cellHash = hash21(cellX, cellY);
    float flash = sin(time * 4.0 + cellHash * 20.0);
    if (flash > 0.8) {
        return vec4(0.9, 0.95, 1.0, 0.7);
    } else if (flash > 0.5) {
        float hue = mod(u + v + time, 3.0);
        vec3 color;
        if (hue < 1.0) color = vec3(1.0, 0.5, 0.5);
        else if (hue < 2.0) color = vec3(0.5, 1.0, 0.5);
        else color = vec3(0.5, 0.5, 1.0);
        return vec4(color, 0.4);
    }
    return vec4(0.0);
}

// 4: holo_runes — Glyphes runiques mystiques clignotants
vec4 holo_runes(float u, float v, int ix, int iy, float time) {
    int runeGridSize = 6;
    int runeX = ix / runeGridSize;
    int runeY = iy / runeGridSize;
    int localX = ix % runeGridSize;
    int localY = iy % runeGridSize;
    if (localX == 0 || localX == runeGridSize - 1 || localY == 0 || localY == runeGridSize - 1) {
        return vec4(0.0);
    }
    int px = localX - 1;
    int py = localY - 1;
    float cellHash = hash21(float(runeX), float(runeY));
    float pulse = sin(time * 2.0 + float(runeX) * 0.3 + float(runeY) * 0.5);
    if (cellHash > 0.4 && pulse > 0.0) {
        int runeType = int(cellHash * 100.0) % 5;
        bool activePixel = false;
        if (runeType == 0) {
            if (px == 0 || px == 3 || py == 0 || py == 3) activePixel = true;
            if (px == 1 && py == 1 && cellHash > 0.8) activePixel = true;
        } else if (runeType == 1) {
            if (py == 0 || py == 3) activePixel = true;
            if (px + py == 3) activePixel = true;
        } else if (runeType == 2) {
            if (px == 1 || px == 2) activePixel = true;
            if (py == 1 || py == 2) activePixel = true;
            if (px == 0 && py == 0) activePixel = false;
            if (px == 3 && py == 3) activePixel = false;
        } else if (runeType == 3) {
            if (py == 0) activePixel = true;
            if (px == 1 || px == 2) activePixel = true;
        } else if (runeType == 4) {
            if (px == 0 || (py == 0 && px > 0)) activePixel = true;
            if (px == 3 && py > 0) activePixel = true;
            if (py == 3 && px > 0) activePixel = true;
            if (px == 1 && py == 2) activePixel = true;
        }
        if (activePixel) {
            vec3 color;
            if (cellHash > 0.8) color = vec3(0.6, 0.2, 1.0);
            else if (cellHash > 0.6) color = vec3(1.0, 0.2, 0.8);
            else color = vec3(0.2, 0.8, 1.0);
            
            float a = 0.6 * pulse;
            float pixelHash = hash21(float(ix) * 3.14, float(iy) * 1.59);
            if (pulse > 0.8 && pixelHash > 0.5) {
                color = vec3(1.0, 1.0, 1.0);
                a = 0.9;
            }
            return vec4(color, a);
        }
    }
    return vec4(0.0);
}

// 5: holo_circuit — Tracé électronique cyberpunk
vec4 holo_circuit(float u, float v, int ix, int iy, float time) {
    float scaleX = 8.0;
    float scaleY = 8.0;
    float offsetX = time * 0.5;
    float offsetY = time * 0.5;
    float gridU = u * scaleX - offsetX;
    float gridV = v * scaleY - offsetY;
    float cellX = floor(gridU);
    float cellY = floor(gridV);
    float localU = gridU - cellX;
    float localV = gridV - cellY;
    float cellHash = hash21(cellX, cellY);
    bool hasHLine = cellHash > 0.5;
    bool hasVLine = fract(cellHash * 10.0) > 0.5;
    float thickness = 0.15;
    bool onHLine = hasHLine && abs(localV - 0.5) < thickness;
    bool onVLine = hasVLine && abs(localU - 0.5) < thickness;
    bool isNode = onHLine || onVLine || (hasHLine && hasVLine && abs(localU - 0.5) < 0.25 && abs(localV - 0.5) < 0.25);
    if (isNode) {
        float dataFlow = sin(cellX * 0.5 + cellY * 0.5 + time * 3.0);
        if (dataFlow > 0.7) {
            return vec4(0.8, 1.0, 0.4, 0.8);
        } else if (dataFlow > 0.0) {
            return vec4(0.3, 0.9, 0.5, 0.5);
        } else {
            return vec4(0.1, 0.4, 0.3, 0.2);
        }
    }
    return vec4(0.0);
}

// 6: holo_bubbles — Bulles d'eau bioluminescentes montantes
vec4 holo_bubbles(float u, float v, int ix, int iy, float time) {
    float b1x = 0.25 + sin(time * 0.8) * 0.05;
    float b1y = 1.2 - mod(time * 0.35, 1.5);
    float r1 = 0.12;

    float b2x = 0.75 + cos(time * 0.6) * 0.08;
    float b2y = 1.2 - mod(time * 0.45, 1.5);
    float r2 = 0.09;

    float b3x = 0.5 + sin(time * 1.1) * 0.12;
    float b3y = 1.2 - mod(time * 0.3, 1.5);
    float r3 = 0.15;

    float b4x = 0.15 + cos(time * 1.3) * 0.05;
    float b4y = 1.2 - mod(time * 0.55, 1.5);
    float r4 = 0.06;

    float b5x = 0.85 + sin(time * 0.9) * 0.04;
    float b5y = 1.2 - mod(time * 0.65, 1.5);
    float r5 = 0.07;

    float d1 = sqrt((u - b1x)*(u - b1x) + (v - b1y)*(v - b1y));
    float d2 = sqrt((u - b2x)*(u - b2x) + (v - b2y)*(v - b2y));
    float d3 = sqrt((u - b3x)*(u - b3x) + (v - b3y)*(v - b3y));
    float d4 = sqrt((u - b4x)*(u - b4x) + (v - b4y)*(v - b4y));
    float d5 = sqrt((u - b5x)*(u - b5x) + (v - b5y)*(v - b5y));

    float d = 1.0;
    float bx = 0.0, by = 0.0, rad = 0.0;
    if (d1 < r1)      { d = d1; bx = b1x; by = b1y; rad = r1; }
    else if (d2 < r2) { d = d2; bx = b2x; by = b2y; rad = r2; }
    else if (d3 < r3) { d = d3; bx = b3x; by = b3y; rad = r3; }
    else if (d4 < r4) { d = d4; bx = b4x; by = b4y; rad = r4; }
    else if (d5 < r5) { d = d5; bx = b5x; by = b5y; rad = r5; }

    if (d < rad) {
        float thickness = 0.03;
        if (d > rad - thickness) {
            return vec4(0.4, 0.8, 1.0, 0.5);
        } else {
            float nx = (u - bx) / rad;
            float ny = (v - by) / rad;
            if (nx < -0.3 && ny < -0.3 && d > rad - thickness - 0.05) {
                return vec4(1.0, 1.0, 1.0, 0.6);
            }
        }
    }
    return vec4(0.0);
}

// 7: holo_shatter — Fissures de glace brisée irisées
vec4 holo_shatter(float u, float v, int ix, int iy, float time) {
    float fracture1 = abs(sin(u * 12.0 + v * 6.0 - time * 0.4));
    float fracture2 = abs(cos(u * 7.0 - v * 14.0 + time * 0.3));
    float fracture3 = abs(sin(u * 4.0 + v * 18.0 + time * 0.6));
    float minFracture = min(fracture1, min(fracture2, fracture3));
    float sweep = sin(u * 6.0 - v * 4.0 + time * 2.5);
    if (minFracture < 0.04) {
        float hueS = mod(u * 2.0 + v * 1.5 + time * 0.8, 3.0);
        vec3 color;
        if (hueS < 1.0) color = vec3(0.7, 0.9, 1.0);
        else if (hueS < 2.0) color = vec3(1.0, 0.8, 0.9);
        else color = vec3(0.8, 1.0, 0.85);
        return vec4(color, 0.7);
    } else if (minFracture < 0.1) {
        float glow = 1.0 - (minFracture - 0.04) / 0.06;
        return vec4(0.6, 0.8, 1.0, 0.25 * glow);
    } else if (sweep > 0.85) {
        return vec4(1.0, 0.95, 0.9, 0.2);
    }
    return vec4(0.0);
}

// 8: holo_ripple — Ondulations de vagues d'énergie dorées
vec4 holo_ripple(float u, float v, int ix, int iy, float time) {
    float wave1 = sin(u * 5.0 + time * 1.5) * 0.15;
    float wave2 = cos(u * 12.0 - time * 0.8) * 0.05;
    float rippleLine = v * 12.0 - u * 4.0 - time * 2.0 + wave1 + wave2;
    float lineInPixel = fract(rippleLine);
    if (lineInPixel > 0.94) {
        return vec4(1.0, 0.95, 0.9, 0.35);
    } else if (lineInPixel > 0.85) {
        float fade = (lineInPixel - 0.85) / 0.09;
        return vec4(0.9, 0.6 + 0.3 * fade, 0.2, 0.15 * fade);
    }
    return vec4(0.0);
}

// 9: holo_scanline — Lignes de balayage technologiques douces
vec4 holo_scanline(float u, float v, int ix, int iy, float time) {
    float scanY1 = mod(v - time * 0.3, 1.0);
    if (scanY1 < 0.0) scanY1 += 1.0;
    float scanY2 = mod(v - time * 0.8, 1.0);
    if (scanY2 < 0.0) scanY2 += 1.0;
    bool onLine1 = scanY1 < 0.05;
    bool onLine2 = scanY2 < 0.033;
    if (onLine1) {
        return vec4(0.4, 1.0, 0.8, 0.3);
    } else if (onLine2) {
        return vec4(1.0, 0.4, 0.8, 0.2);
    } else {
        if (ix % 4 == 0 || iy % 4 == 0) {
            float a = 0.05;
            vec3 color = vec3(0.8, 0.9, 1.0);
            if (ix % 4 == 0 && iy % 4 == 0) {
                float hash = fract(abs(sin(float(ix) * 12.9898 + float(iy) * 78.233 + time)));
                if (hash > 0.95) {
                    color = vec3(1.0);
                    a = 0.4;
                }
            }
            return vec4(color, a);
        }
    }
    return vec4(0.0);
}

// 10: holo_prism — Facettes de prisme triangulaires irrisées
vec4 holo_prism(float u, float v, int ix, int iy, float time) {
    float prismU = u * 6.0;
    float prismV = v * 5.0;
    int rowP = int(floor(prismV));
    if (rowP % 2 == 0) prismU += 0.5;
    int colP = int(floor(prismU));
    float localPU = prismU - float(colP);
    float localPV = prismV - float(rowP);
    bool upperTriangle = (localPU + localPV) < 1.0;
    int triId = colP * 2 + rowP * 13 + (upperTriangle ? 0 : 1);
    float facetHash = hash11(float(triId) * 12.9898 + 78.233);
    float sweepP = sin(u * 8.0 - v * 3.0 + time * 2.5);
    float edgeDistH = min(localPV, 1.0 - localPV);
    float edgeDistV = min(localPU, 1.0 - localPU);
    float edgeDistDiag = abs(localPU + localPV - 1.0) / 1.414;
    float minEdgeDist = min(edgeDistH, min(edgeDistV, edgeDistDiag));
    float hueP = mod(facetHash * 3.0 + time * 0.8, 3.0);
    vec3 color;
    if (hueP < 1.0) color = vec3(0.7, 0.9, 1.0);
    else if (hueP < 2.0) color = vec3(1.0, 0.7, 0.9);
    else color = vec3(0.9, 1.0, 0.7);

    if (minEdgeDist < 0.06) {
        float edgeIntensity = 1.0 - minEdgeDist / 0.06;
        return vec4(0.9, 0.95, 1.0, 0.5 * edgeIntensity);
    } else if (sweepP > 0.8 && minEdgeDist > 0.15) {
        float sweepIntensity = (sweepP - 0.8) / 0.2;
        return vec4(color, 0.3 * sweepIntensity);
    }
    return vec4(0.0);
}

// 11: holo_aurora — Voiles d'aurore boréale fluorescents
vec4 holo_aurora(float u, float v, int ix, int iy, float time) {
    float wave1A = sin(u * 3.0 + time * 0.6) * 0.15;
    float wave2A = sin(u * 7.0 - time * 1.2) * 0.08;
    float wave3A = cos(u * 5.0 + time * 0.9) * 0.1;
    float curtain1 = v - 0.3 + wave1A + wave2A;
    float curtain2 = v - 0.5 + wave2A + wave3A;
    float curtain3 = v - 0.7 + wave1A + wave3A;
    float i1 = max(0.0, 1.0 - abs(curtain1) * 8.0);
    float i2 = max(0.0, 1.0 - abs(curtain2) * 10.0);
    float i3 = max(0.0, 1.0 - abs(curtain3) * 8.0);
    float shimmer = sin(v * 40.0 + u * 5.0 + time * 3.0) * 0.5 + 0.5;
    i1 *= (0.7 + shimmer * 0.3);
    i2 *= (0.7 + shimmer * 0.3);
    i3 *= (0.7 + shimmer * 0.3);
    float rA = i1 * 0.2 + i2 * 0.5 + i3 * 0.8;
    float gA = i1 * 1.0 + i2 * 0.3 + i3 * 0.2;
    float bA = i1 * 0.4 + i2 * 0.9 + i3 * 1.0;
    vec3 color = vec3(min(1.0, rA), min(1.0, gA), min(1.0, bA));
    float totalIntensity = min(1.0, i1 + i2 + i3);
    float a = 0.0;
    if (totalIntensity > 0.7) a = 0.6;
    else if (totalIntensity > 0.3) a = 0.35 * totalIntensity;
    else if (totalIntensity > 0.05) a = 0.15 * totalIntensity;
    return vec4(color, a);
}

// 12: holo_vortex — Spirale cosmique d'énergie
vec4 holo_vortex(float u, float v, int ix, int iy, float time) {
    float dxV = u - 0.5;
    float dyV = v - 0.5;
    float angleV = atan(dyV, dxV);
    float radiusV = sqrt(dxV * dxV + dyV * dyV);
    float PI = 3.14159265;
    float spiral1 = angleV * 2.0 - radiusV * 18.0 + time * 3.0;
    float spiral2 = angleV * 2.0 - radiusV * 18.0 + time * 3.0 + PI * 0.66;
    float spiral3 = angleV * 2.0 - radiusV * 18.0 + time * 3.0 + PI * 1.33;
    float band1 = fract(spiral1);
    float band2 = fract(spiral2);
    float band3 = fract(spiral3);
    float arm1 = band1 > 0.85 ? (band1 - 0.85) / 0.15 : (band1 > 0.75 ? (0.85 - band1) / 0.10 * 0.4 : 0.0);
    float arm2 = band2 > 0.85 ? (band2 - 0.85) / 0.15 : (band2 > 0.75 ? (0.85 - band2) / 0.10 * 0.4 : 0.0);
    float arm3 = band3 > 0.85 ? (band3 - 0.85) / 0.15 : (band3 > 0.75 ? (0.85 - band3) / 0.10 * 0.4 : 0.0);
    float radialFade = max(0.0, 1.0 - radiusV * 1.8);
    arm1 *= radialFade;
    arm2 *= radialFade;
    arm3 *= radialFade;
    float hueV = mod(angleV / (PI * 2.0) + 0.5 + time * 0.2, 1.0);
    if (hueV < 0.0) hueV += 1.0;
    vec3 color;
    if (hueV < 0.33) {
        float t2 = hueV / 0.33;
        color = vec3(0.5 * (1.0 - t2), 0.1 + 0.7 * t2, 0.9 + 0.1 * t2);
    } else if (hueV < 0.66) {
        float t2 = (hueV - 0.33) / 0.33;
        color = vec3(0.0 + 1.0 * t2, 0.8 * (1.0 - t2), 1.0 - 0.2 * t2);
    } else {
        float t2 = (hueV - 0.66) / 0.34;
        color = vec3(1.0 - 0.5 * t2, 0.0 + 0.1 * t2, 0.8 + 0.1 * t2);
    }
    float totalArm = min(1.0, arm1 + arm2 + arm3);
    float centerGlow = max(0.0, 1.0 - radiusV * 8.0);
    if (centerGlow > 0.3) {
        return vec4(1.0, 0.95, 1.0, 0.5 * centerGlow);
    } else if (totalArm > 0.05) {
        if (totalArm > 0.7) {
            float whiteMix = (totalArm - 0.7) / 0.3;
            color = mix(color, vec3(1.0), whiteMix * 0.5);
        }
        return vec4(color, 0.5 * totalArm);
    }
    return vec4(0.0);
}

// 13: holo_lightning — Éclats d'éclairs crépitants
vec4 holo_lightning(float u, float v, int ix, int iy, float time) {
    float r = 1.0, g = 1.0, b = 1.0, a = 0.0;
    for (int bolt = 0; bolt < 3; bolt++) {
        float boltTime = time + float(bolt) * 3.33;
        float boltPhase = mod(boltTime * 1.5, 3.0);
        float flashIntensity = 0.0;
        if (boltPhase < 0.15) {
            flashIntensity = 1.0;
        } else if (boltPhase < 0.25) {
            flashIntensity = 0.5;
        } else if (boltPhase > 1.0 && boltPhase < 1.1) {
            flashIntensity = 0.7;
        }
        if (flashIntensity > 0.0) {
            float boltSeed = fract(abs(sin(float(bolt) * 45.678 + floor(boltTime * 0.5) * 12.345)));
            float startX = 0.1 + boltSeed * 0.8;
            float boltX = startX;
            for (int scanRow = 0; scanRow < 30; scanRow++) {
                if (scanRow > iy) break;
                float rowHash = fract(abs(sin(float(scanRow) * 7.654 + float(bolt) * 23.456 + floor(boltTime * 0.5) * 5.678)));
                boltX += (rowHash - 0.5) * 0.08;
            }
            float distToBolt = abs(u - boltX);
            if (distToBolt < 0.025) {
                r = 1.0; g = 1.0; b = 1.0;
                a = max(a, 0.85 * flashIntensity);
            } else if (distToBolt < 0.06) {
                r = 0.4; g = 0.7; b = 1.0;
                a = max(a, 0.5 * flashIntensity * (1.0 - (distToBolt - 0.025) / 0.035));
            } else if (distToBolt < 0.1) {
                if (a < 0.1) {
                    r = 0.3; g = 0.1; b = 0.6;
                    a = 0.15 * flashIntensity * (1.0 - (distToBolt - 0.06) / 0.04);
                }
            }
            if (iy % 5 == 0 && distToBolt < 0.15) {
                float branchHash = fract(abs(sin(float(iy) * 3.14 + float(bolt) * 9.87 + floor(boltTime * 0.5) * 4.56)));
                if (branchHash > 0.4) {
                    float branchDir = branchHash > 0.7 ? 1.0 : -1.0;
                    float distBranch = abs(u - (boltX + branchDir * 0.05 * float(iy % 5)));
                    if (distBranch < 0.02) {
                        r = 0.7; g = 0.85; b = 1.0;
                        a = max(a, 0.5 * flashIntensity);
                    }
                }
            }
        }
    }
    return vec4(r, g, b, a);
}

// 14: holo_galaxy — Étoiles scintillantes et nébuleuse cosmique
vec4 holo_galaxy(float u, float v, int ix, int iy, float time) {
    float cloud1 = sin(u * 6.0 + time * 0.4) * cos(v * 5.0 - time * 0.3);
    float cloud2 = sin(u * 11.0 - time * 0.7 + v * 3.0) * 0.5;
    float cloud3 = cos((u + v) * 8.0 + time * 0.5) * 0.3;
    float nebula = (cloud1 + cloud2 + cloud3) / 1.8;
    float starSeed = float(ix) * 127.1 + float(iy) * 311.7;
    float starHash = fract(abs(sin(starSeed)));
    float twinkle = sin(time * 5.0 + starHash * 20.0);
    if (starHash > 0.985) {
        float starIntensity = float(twinkle * 0.5 + 0.5);
        return vec4(0.85 + 0.15 * starIntensity, 0.9 + 0.1 * starIntensity, 1.0, 0.7 * starIntensity);
    } else if (starHash > 0.975) {
        return vec4(0.7, 0.85, 1.0, 0.4 * float(twinkle * 0.4 + 0.6));
    } else if (nebula > 0.6) {
        float t2 = (nebula - 0.6) / 0.4;
        return vec4(0.9, 0.2 + t2 * 0.3, 0.8 + t2 * 0.2, 0.08 * t2);
    } else if (nebula > 0.3) {
        float t2 = (nebula - 0.3) / 0.3;
        return vec4(0.3, 0.5 + t2 * 0.3, 1.0, 0.05 * t2);
    }
    return vec4(0.0);
}

// 15: holo_sakura — Pétales de cerisier tombant doucement
vec4 holo_sakura(float u, float v, int ix, int iy, float time) {
    float r = 1.0, g = 0.55, b = 0.7, a = 0.0;
    // Petals array: startX, phase, rad, fallSpeed, swaySpeed
    float petals[30] = float[](
        0.15, 0.55, 0.07, 0.65, 0.5,
        0.40, 0.20, 0.06, 0.45, 0.7,
        0.65, 0.80, 0.08, 0.55, 0.4,
        0.85, 0.40, 0.05, 0.70, 0.9,
        0.30, 0.95, 0.09, 0.40, 0.6,
        0.75, 0.10, 0.06, 0.60, 0.8
    );
    for (int i = 0; i < 6; i++) {
        float pStartX = petals[i * 5 + 0];
        float pPhase  = petals[i * 5 + 1];
        float pRad    = petals[i * 5 + 2];
        float pFall   = petals[i * 5 + 3];
        float pSway   = petals[i * 5 + 4];
        
        float py = mod(pPhase + time * pFall, 1.2) - 0.1;
        float px = pStartX + sin(time * pSway + pPhase * 6.28) * 0.08;
        float rot = time * pSway * 0.5 + pPhase * 3.14;
        float cosR = cos(rot);
        float sinR = sin(rot);
        float dx = (u - px) * cosR + (v - py) * sinR;
        float dy = -(u - px) * sinR + (v - py) * cosR;
        float petalDist = sqrt((dx / pRad) * (dx / pRad) + (dy / (pRad * 1.6)) * (dy / (pRad * 1.6)));
        if (petalDist < 1.0) {
            float edge = 1.0 - petalDist;
            float lightness = 0.7 + edge * 0.3;
            r = 1.0 * lightness;
            g = 0.55 * lightness;
            b = 0.7 * lightness;
            a = max(a, 0.55 * edge * edge);
        } else if (petalDist < 1.15) {
            float glow = 1.0 - (petalDist - 1.0) / 0.15;
            if (a < 0.1) {
                r = 1.0; g = 0.7; b = 0.8;
                a = 0.15 * glow;
            }
        }
    }
    return vec4(r, g, b, a);
}

// 16: holo_plasma_arc — Arcs de plasma courbés distribués sur toute la carte
vec4 holo_plasma_arc(float u, float v, int ix, int iy, float time) {
    float r = 1.0, g = 1.0, b = 1.0, a = 0.0;
    for (int arc = 0; arc < 4; arc++) {
        float arcSeed = float(arc) * 0.25;
        float arcTime = time * 0.8 + arcSeed * 6.28;
        
        float arcAnchorX = 0.15 + float(arc) * 0.22 + sin(arcTime * 0.3) * 0.05;
        float arcAnchorY = (arc == 0 || arc == 2) ? 0.95 : 0.05;
        
        float arcPeakX = arcAnchorX + sin(arcTime * 0.7 + arcSeed) * 0.18;
        float arcPeakY = 0.3 + float(arc) * 0.12 + cos(arcTime * 0.5 + arcSeed) * 0.15;
        
        float arcEndX = arcAnchorX + sin(arcTime * 0.6 + arcSeed + 1.0) * 0.18;
        float arcEndY = (arc == 0 || arc == 3) ? 0.05 : 0.95;
        
        float minArcDist = 9999.0;
        for (int s = 0; s <= 20; s++) {
            float t2 = float(s) / 20.0;
            float bx = (1.0-t2)*(1.0-t2)*arcAnchorX + 2.0*(1.0-t2)*t2*arcPeakX + t2*t2*arcEndX;
            float by = (1.0-t2)*(1.0-t2)*arcAnchorY + 2.0*(1.0-t2)*t2*arcPeakY + t2*t2*arcEndY;
            float ddx = u - bx, ddy = v - by;
            float dist = sqrt(ddx*ddx + ddy*ddy);
            if (dist < minArcDist) minArcDist = dist;
        }
        
        float pulse = sin(arcTime * 3.0 + float(arc)) * 0.5 + 0.5;
        if (minArcDist < 0.015) {
            r = 1.0; g = 0.9; b = 0.5;
            a = max(a, 0.65 * pulse);
        } else if (minArcDist < 0.035) {
            float fade = 1.0 - (minArcDist - 0.015) / 0.02;
            r = 1.0; g = 0.4; b = 0.1;
            a = max(a, 0.45 * fade * pulse);
        } else if (minArcDist < 0.06) {
            float fade = 1.0 - (minArcDist - 0.035) / 0.025;
            if (a < 0.15) {
                r = 0.8; g = 0.1; b = 0.5;
                a = 0.15 * fade * pulse;
            }
        }
    }
    return vec4(r, g, b, a);
}

// 17: holo_diamond — Reflets irisés facettés style diamant (très discret pour visibilité)
vec4 holo_diamond(float u, float v, int ix, int iy, float time) {
    float du = (u - v) * 4.0;
    float dv = (u + v) * 4.0;
    float cellU = floor(du);
    float cellV = floor(dv);
    float lu = du - cellU;
    float lv = dv - cellV;
    float fHash = hash21(cellU, cellV);
    float lightAngle = time * 1.2;
    float nx = (fHash - 0.5) * 0.6;
    float ny = (fract(fHash * 7.0) - 0.5) * 0.6;
    float nz = sqrt(max(0.0, 1.0 - nx*nx - ny*ny));
    float specular = max(0.0, cos(lightAngle) * nx + sin(lightAngle) * ny + 0.8 * nz);
    float specPow = specular * specular * specular * specular;
    float minEdge = min(min(lu, 1.0 - lu), min(lv, 1.0 - lv));
    if (minEdge < 0.05) {
        float hue = mod(fHash * 3.0 + time * 0.5, 3.0);
        vec3 color;
        if (hue < 1.0) color = vec3(0.8, 0.9, 1.0);
        else if (hue < 2.0) color = vec3(1.0, 0.8, 0.9);
        else color = vec3(0.9, 1.0, 0.8);
        return vec4(color, 0.12 * (1.0 - minEdge / 0.05));
    } else if (specPow > 0.3) {
        return vec4(1.0, 0.98, 0.95, 0.25 * (specPow - 0.3) / 0.7);
    } else if (specPow > 0.05) {
        float hue = mod(fHash * 3.0 + u + v + time * 0.3, 3.0);
        vec3 color;
        if (hue < 1.0) color = vec3(0.6, 0.8, 1.0);
        else if (hue < 2.0) color = vec3(1.0, 0.6, 0.8);
        else color = vec3(0.8, 1.0, 0.6);
        return vec4(color, 0.08 * specPow);
    }
    return vec4(0.0);
}

// 18: holo_aura — Soft premium waving/shimmering border aura glow
vec4 holo_aura(float u, float v, int ix, int iy, float time) {
    float distToEdge = min(min(u, 1.0 - u), min(v, 1.0 - v));
    float wave = sin(u * 12.0 + time * 2.5) * cos(v * 12.0 - time * 1.8) * 0.012;
    float d = distToEdge + wave;
    
    if (d < 0.08) {
        float pulse = sin(time * 2.0 - distToEdge * 15.0) * 0.2 + 0.8;
        float alpha = smoothstep(0.08, 0.0, d) * 0.45 * pulse;
        
        float colorShift = mod(time * 0.5 + distToEdge, 3.0);
        vec3 color;
        if (colorShift < 1.0) {
            color = mix(vec3(0.0, 0.8, 1.0), vec3(0.6, 0.2, 1.0), colorShift);
        } else if (colorShift < 2.0) {
            color = mix(vec3(0.6, 0.2, 1.0), vec3(1.0, 0.8, 0.2), colorShift - 1.0);
        } else {
            color = mix(vec3(1.0, 0.8, 0.2), vec3(0.0, 0.8, 1.0), colorShift - 2.0);
        }
        return vec4(color, alpha);
    }
    return vec4(0.0);
}

// 19: holo_neon_pulse — Grille néon cyberpunk avec noeuds scintillants
vec4 holo_neon_pulse(float u, float v, int ix, int iy, float time) {
    float scrollU = u * 7.0 + time * 0.3;
    float scrollV = v * 7.0 - time * 0.5;
    float lineU = scrollU - floor(scrollU);
    float lineV = scrollV - floor(scrollV);
    float minLine = min(abs(lineU - 0.5), abs(lineV - 0.5));
    float nodeDistU = min(lineU, 1.0 - lineU);
    float nodeDistV = min(lineV, 1.0 - lineV);
    float nodeDist = sqrt(nodeDistU*nodeDistU + nodeDistV*nodeDistV);
    float hueN = mod(u * 2.0 + time * 0.4, 3.0);
    vec3 color;
    if (hueN < 1.0) {
        color = vec3(0.0, 1.0, 0.8 + 0.2*hueN);
    } else if (hueN < 2.0) {
        float t2 = hueN - 1.0;
        color = vec3(0.8*t2, 0.2, 1.0);
    } else {
        float t2 = hueN - 2.0;
        color = vec3(0.8, 0.2 + 0.8*t2, 1.0 - 0.2*t2);
    }
    float dxC = u - 0.5;
    float dyC = v - 0.5;
    float radialPulse = sin(sqrt(dxC*dxC + dyC*dyC) * 12.0 - time * 4.0) * 0.5 + 0.5;
    
    if (nodeDist < 0.08) {
        float nodeHash = fract(abs(sin(floor(scrollU) * 13.7 + floor(scrollV) * 41.3)));
        float nodeFlash = sin(time * 6.0 + nodeHash * 20.0) * 0.5 + 0.5;
        float nodeIntensity = (1.0 - nodeDist / 0.08) * nodeFlash;
        return vec4(min(vec3(1.0), color + 0.4), 0.8 * nodeIntensity);
    } else if (minLine < 0.06) {
        return vec4(color, 0.4 * (1.0 - minLine / 0.06) * (0.5 + 0.5 * radialPulse));
    }
    return vec4(0.0);
}


// 20: holo_constellation — Constellations scintillantes reliées par des lignes d'énergie
vec4 holo_constellation(float u, float v, int ix, int iy, float time) {
    float minStarDist = 9999.0;
    float minLineDist = 9999.0;
    float starXs[8] = float[](0.2, 0.35, 0.45, 0.7, 0.8, 0.6, 0.3, 0.15);
    float starYs[8] = float[](0.25, 0.45, 0.3, 0.35, 0.6, 0.75, 0.7, 0.55);
    for (int s = 0; s < 8; s++) {
        float sx = starXs[s];
        float sy = starYs[s];
        float dist = sqrt((u - sx)*(u - sx) + (v - sy)*(v - sy));
        if (dist < minStarDist) minStarDist = dist;
        int next = int(mod(float(s + 1), 8.0));
        float nx = starXs[next];
        float ny = starYs[next];
        float l2 = (nx - sx)*(nx - sx) + (ny - sy)*(ny - sy);
        float t_proj = clamp(((u - sx)*(nx - sx) + (v - sy)*(ny - sy)) / l2, 0.0, 1.0);
        float projX = sx + t_proj * (nx - sx);
        float projY = sy + t_proj * (ny - sy);
        float distLine = sqrt((u - projX)*(u - projX) + (v - projY)*(v - projY));
        if (distLine < minLineDist) minLineDist = distLine;
    }
    float starPulse = sin(time * 3.0 + u * 10.0 + v * 10.0) * 0.4 + 0.6;
    if (minStarDist < 0.02) {
        return vec4(0.8, 0.95, 1.0, 0.8 * starPulse);
    } else if (minStarDist < 0.05) {
        float fade = 1.0 - (minStarDist - 0.02) / 0.03;
        return vec4(0.4, 0.8, 1.0, 0.4 * fade * starPulse);
    } else if (minLineDist < 0.012) {
        return vec4(0.3, 0.7, 1.0, 0.25 * (sin(time * 1.5 + (u + v) * 4.0) * 0.3 + 0.7));
    }
    return vec4(0.0);
}

// 21: holo_cyber_dust — Particules numériques carrées défilant sur toute la carte
vec4 holo_cyber_dust(float u, float v, int ix, int iy, float time) {
    float speed = 0.15;
    for (int p = 0; p < 12; p++) {
        float seedX = fract(abs(sin(float(p) * 23.45)) * 0.9) + 0.05;
        float seedY = fract(abs(sin(float(p) * 45.67)) * 0.9) + 0.05;
        float seedSpd = 0.5 + fract(abs(sin(float(p) * 12.34))) * 0.5;
        float px = seedX;
        float py = mod(seedY - time * speed * seedSpd, 1.0);
        if (py < 0.0) py += 1.0;
        
        if (abs(u - px) < 0.025 && abs(v - py) < 0.025) {
            float pTime = time * 4.0 + float(p) * 2.0;
            float flash = sin(pTime) * 0.5 + 0.5;
            float hue = mod(float(p) * 0.3 + time * 0.5, 3.0);
            vec3 color;
            if (hue < 1.0) color = vec3(0.1, 0.9, 1.0);
            else if (hue < 2.0) color = vec3(1.0, 0.2, 0.9);
            else color = vec3(0.2, 0.4, 1.0);
            
            float edgeFade = smoothstep(0.0, 0.12, py) * smoothstep(1.0, 0.88, py);
            return vec4(color, 0.65 * flash * edgeFade);
        }
    }
    return vec4(0.0);
}

// 22: holo_magical_wind — Ondulations féeriques pastel
vec4 holo_magical_wind(float u, float v, int ix, int iy, float time) {
    float wave1 = sin(u * 4.0 - time * 2.0) * 0.12;
    float wave2 = cos(u * 8.0 + time * 1.2) * 0.05;
    float ribbonY1 = v - 0.35 + wave1 + wave2;
    float ribbonY2 = v - 0.65 - wave1 + wave2;
    float intensity1 = exp(-pow(ribbonY1 * 12.0, 2.0));
    float intensity2 = exp(-pow(ribbonY2 * 10.0, 2.0));
    float totalIntensity = intensity1 + intensity2;
    if (totalIntensity > 0.05) {
        float hue = mod(u * 1.5 + time * 0.6, 3.0);
        vec3 color;
        if (hue < 1.0) color = vec3(0.8, 0.95, 1.0);
        else if (hue < 2.0) color = vec3(1.0, 0.8, 0.95);
        else color = vec3(0.9, 0.8, 1.0);
        return vec4(color, 0.35 * totalIntensity * (sin(time * 2.0 + u * 3.0) * 0.3 + 0.7));
    }
    return vec4(0.0);
}

// 23: holo_mega — Pulsing purple-magenta DNA helix lines (reworked to protect central sprite)
vec4 holo_mega(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = (v - 0.5) * 1.3;
    float radius = sqrt(dx * dx + dy * dy);
    
    float pulse = sin(time * 4.0 - radius * 10.0) * 0.5 + 0.5;
    
    // Thinner DNA strands (approx 1 pixel wide in 40x30 grid) to look more elegant
    bool isDna = abs(dx - sin(dy * 12.0 + time * 2.0) * 0.15) < 0.025
              || abs(dx + sin(dy * 12.0 + time * 2.0) * 0.15) < 0.025;
              
    if (isDna && radius < 0.35) {
        // Protect the center where the Pokemon's face/body is by fading it out
        float centerFade = clamp((radius - 0.10) / 0.20, 0.0, 1.0);
        float dnaAlpha = 0.45 * centerFade;
        if (dnaAlpha > 0.01) {
            return vec4(1.0, 0.2, 0.8, dnaAlpha);
        }
    } else if (abs(radius - 0.22) < 0.025) {
        // Soft pulsing ring that also fades slightly near the center
        float ringFade = clamp((radius - 0.10) / 0.15, 0.0, 1.0);
        return vec4(0.8, 0.1, 0.9, 0.35 * pulse * ringFade);
    }
    return vec4(0.0);
}

// 24: holo_regional — Floating glowing turquoise runes
vec4 holo_regional(float u, float v, int ix, int iy, float time) {
    float su = mod(u + v * 0.5 - time * 0.4, 0.4);
    float sv = mod(v - u * 0.2 - time * 0.1, 0.3);
    if (su < 0.0) su += 0.4;
    if (sv < 0.0) sv += 0.3;
    
    bool rune = (abs(su - 0.2) < 0.02 && abs(sv - 0.15) < 0.1)
             || (abs(sv - 0.15) < 0.02 && abs(su - 0.2) < 0.08);
             
    if (rune) {
        return vec4(0.1, 0.9, 1.0, 0.75);
    }
    return vec4(0.0);
}

// ===== MAIN =====

// 25: holo_time_gears — Rotating transparent golden gear outlines and pulsing time waves (Dialga theme)
vec4 holo_time_gears(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = (v - 0.5) * 1.3;
    float radius = sqrt(dx * dx + dy * dy);
    float theta = atan(dy, dx);
    
    float wave = fract(radius * 3.0 - time * 0.8);
    float pulse = smoothstep(0.9, 1.0, wave) * smoothstep(1.0, 0.9, wave) * max(0.0, 1.0 - radius * 1.5);
    
    float gearAngle = theta - time * 0.6;
    float gearTeeth = sin(gearAngle * 10.0) * 0.02;
    float gear1 = abs(radius - 0.28 - gearTeeth);
    
    float gearAngle2 = theta + time * 0.4 + 1.5;
    float gearTeeth2 = sin(gearAngle2 * 8.0) * 0.015;
    float gear2 = abs(radius - 0.16 - gearTeeth2);
    
    float alpha = 0.0;
    vec3 color = vec3(0.0);
    
    if (gear1 < 0.018) {
        alpha = 0.55 * (1.0 - gear1 / 0.018);
        color = vec3(0.95, 0.75, 0.2);
    } else if (gear2 < 0.015) {
        alpha = 0.45 * (1.0 - gear2 / 0.015);
        color = vec3(0.95, 0.75, 0.2);
    } else if (pulse > 0.05) {
        alpha = pulse * 0.35;
        color = vec3(0.1, 0.85, 1.0);
    }
    
    float centerFade = clamp((radius - 0.08) / 0.15, 0.0, 1.0);
    return vec4(color, alpha * centerFade);
}

// 26: holo_spatial_crack — Glowing violet spatial cracks with bleeding purple-magenta star dust (Palkia theme)
vec4 holo_spatial_crack(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = (v - 0.5) * 1.3;
    float radius = sqrt(dx * dx + dy * dy);
    
    float pulse = sin(time * 3.5) * 0.15 + 0.85;
    
    float crack1 = abs(sin(u * 14.0 + v * 9.0 + sin(time * 0.5) * 0.4));
    float crack2 = abs(cos(u * 8.0 - v * 16.0 - cos(time * 0.6) * 0.3));
    float minCrack = min(crack1, crack2);
    
    float alpha = 0.0;
    vec3 color = vec3(0.0);
    
    if (minCrack < 0.022 && radius > 0.1) {
        alpha = 0.85 * (1.0 - minCrack / 0.022) * pulse;
        color = mix(vec3(0.7, 0.15, 1.0), vec3(1.0, 0.9, 1.0), 1.0 - minCrack / 0.022);
    } else {
        float dust = sin(u * 20.0 + time * 1.0) * cos(v * 20.0 - time * 0.8);
        if (minCrack < 0.12 && dust > 0.68 && radius > 0.1) {
            alpha = 0.4 * (1.0 - minCrack / 0.12) * ((dust - 0.68) / 0.32);
            color = vec3(0.9, 0.05, 0.6);
        }
    }
    
    float centerFade = clamp((radius - 0.08) / 0.15, 0.0, 1.0);
    return vec4(color, alpha * centerFade);
}

// 27: holo_prism_stars — Glistening rotating 3D triangular crystal fragments reflecting color-shifting rainbows (Necrozma theme)
vec4 holo_prism_stars(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = (v - 0.5) * 1.3;
    float radius = sqrt(dx * dx + dy * dy);
    
    float alpha = 0.0;
    vec3 color = vec3(0.0);
    
    float scaleX = 4.0;
    float scaleY = 4.0;
    float gridU = u * scaleX;
    float gridV = v * scaleY + time * 0.15;
    
    float cellU = floor(gridU);
    float cellV = floor(gridV);
    float lu = gridU - cellU;
    float lv = gridV - cellV;
    
    float cellHash = hash21(cellU, cellV);
    
    if (cellHash > 0.7 && radius > 0.12) {
        float rot = time * 0.8 + cellHash * 6.28;
        float cosR = cos(rot);
        float sinR = sin(rot);
        
        float rx = (lu - 0.5) * cosR + (lv - 0.5) * sinR;
        float ry = -(lu - 0.5) * sinR + (lv - 0.5) * cosR;
        
        float shard = abs(rx) + ry * 0.5;
        float shine = sin(time * 3.0 + cellHash * 20.0) * 0.5 + 0.5;
        
        if (shard < 0.22) {
            alpha = 0.65 * (1.0 - shard / 0.22);
            float hue = mod(u + v + time * 0.6 + cellHash, 3.0);
            if (hue < 1.0) {
                color = mix(vec3(1.0, 0.2, 0.4), vec3(0.2, 0.8, 1.0), hue);
            } else if (hue < 2.0) {
                color = mix(vec3(0.2, 0.8, 1.0), vec3(0.9, 0.9, 0.2), hue - 1.0);
            } else {
                color = mix(vec3(0.9, 0.9, 0.2), vec3(1.0, 0.2, 0.4), hue - 2.0);
            }
            if (shine > 0.8) {
                color = mix(color, vec3(1.0, 1.0, 1.0), (shine - 0.8) / 0.2);
            }
        }
    }
    
    float centerFade = clamp((radius - 0.08) / 0.15, 0.0, 1.0);
    return vec4(color, alpha * centerFade);
}

void main() {
    // Récupérer l'ID d'effet depuis le canal rouge de la couleur de vertex
    int effectId = int(vertexColor.r * 255.0 + 0.5);

    // Reconstruire le temps réel depuis G et B (16-bit millisecondes)
    float time = (vertexColor.g * 255.0 * 256.0 + vertexColor.b * 255.0) / 1000.0;

    // Calculer les coordonnées pixel-art (grille 40×30)
    float rawU = texCoord0.x;
    float rawV = texCoord0.y;
    int ix = int(floor(rawU * 40.0));
    int iy = int(floor(rawV * 30.0));
    // Quantifier pour l'aspect pixel-art
    float u = float(ix) / 40.0;
    float v = float(iy) / 30.0;

    vec4 color = vec4(0.0);

    // Dispatch vers l'effet approprié
    if      (effectId == 0)  color = holo_lines(u, v, ix, iy, time);
    else if (effectId == 1)  color = holo_pulse(u, v, ix, iy, time);
    else if (effectId == 2)  color = holo_rainbow(u, v, ix, iy, time);
    else if (effectId == 3)  color = holo_sparkle(u, v, ix, iy, time);
    else if (effectId == 4)  color = holo_runes(u, v, ix, iy, time);
    else if (effectId == 5)  color = holo_circuit(u, v, ix, iy, time);
    else if (effectId == 6)  color = holo_bubbles(u, v, ix, iy, time);
    else if (effectId == 7)  color = holo_shatter(u, v, ix, iy, time);
    else if (effectId == 8)  color = holo_ripple(u, v, ix, iy, time);
    else if (effectId == 9)  color = holo_scanline(u, v, ix, iy, time);
    else if (effectId == 10) color = holo_prism(u, v, ix, iy, time);
    else if (effectId == 11) color = holo_aurora(u, v, ix, iy, time);
    else if (effectId == 12) color = holo_vortex(u, v, ix, iy, time);
    else if (effectId == 13) color = holo_lightning(u, v, ix, iy, time);
    else if (effectId == 14) color = holo_galaxy(u, v, ix, iy, time);
    else if (effectId == 15) color = holo_sakura(u, v, ix, iy, time);
    else if (effectId == 16) color = holo_plasma_arc(u, v, ix, iy, time);
    else if (effectId == 17) color = holo_diamond(u, v, ix, iy, time);
    else if (effectId == 18) color = holo_aura(u, v, ix, iy, time);
    else if (effectId == 19) color = holo_neon_pulse(u, v, ix, iy, time);
    else if (effectId == 20) color = holo_constellation(u, v, ix, iy, time);
    else if (effectId == 21) color = holo_cyber_dust(u, v, ix, iy, time);
    else if (effectId == 22) color = holo_magical_wind(u, v, ix, iy, time);
    else if (effectId == 23) color = holo_mega(u, v, ix, iy, time);
    else if (effectId == 24) color = holo_regional(u, v, ix, iy, time);
    else if (effectId == 25) color = holo_time_gears(u, v, ix, iy, time);
    else if (effectId == 26) color = holo_spatial_crack(u, v, ix, iy, time);
    else if (effectId == 27) color = holo_prism_stars(u, v, ix, iy, time);

    fragColor = clamp(color, 0.0, 1.0);
    if (fragColor.a < 0.01) {
        discard;
    }
}

#version 150

// ============================================================================
// Fragment Shader — Arrière-plans procéduraux pour CobblemonCards
// 21 effets portés depuis Java, exécutés entièrement sur GPU.
// L'effectId est encodé dans vertexColor.r (0-20).
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

// 0: water_anim — Vagues d'eau animées
vec3 bg_water_anim(float u, float v, int ix, int iy, float time) {
    float wave1 = sin(u * 15.0 - time * 2.0) * 0.1;
    float wave2 = sin(u * 5.0 + time * 1.5) * 0.05;
    float waterLevel = v + wave1 + wave2;
    float repeatLevel = mod(waterLevel * 5.0 - time, 1.0);
    if (repeatLevel < 0.0) repeatLevel += 1.0;
    if (repeatLevel > 0.85) return vec3(0.85, 0.95, 1.0);
    else if (repeatLevel > 0.75) return vec3(0.3, 0.7, 0.95);
    else return vec3(0.1, 0.45, 0.85);
}

// 1: lava_anim — Lave bouillonnante
vec3 bg_lava_anim(float u, float v, int ix, int iy, float time) {
    float noise = sin(u * 8.0 - time * 2.0) * sin(v * 8.0 + time * 2.5)
                + sin((u - v) * 12.0 + time);
    if (noise > 0.8) return vec3(1.0, 0.8, 0.0);
    else if (noise > 0.2) return vec3(0.9, 0.3, 0.0);
    else if (noise > -0.4) return vec3(0.6, 0.1, 0.0);
    else return vec3(0.3, 0.1, 0.1);
}

// 2: balatro_swirl — Psychedelic Balatro waves
vec3 bg_balatro_swirl(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = (v - 0.5) * 1.4; // Aspect ratio adjustment
    float r = sqrt(dx * dx + dy * dy);
    float theta = atan(dy, dx);
    
    // Wave patterns
    float wave = sin(r * 16.0 - time * 2.5 + sin(theta * 4.0 + time * 1.2) * 1.2);
    float wave2 = cos(theta * 3.0 - time * 1.8 + r * 10.0);
    float val = wave + wave2 * 0.6;
    
    // Psychedelic Balatro high-contrast palette matching
    if (val > 0.4) {
        return vec3(0.9, 0.15, 0.1); // Bright Balatro Red/Orange
    } else if (val > 0.0) {
        return vec3(0.6, 0.05, 0.4); // Darker Magenta/Pink
    } else if (val > -0.6) {
        return vec3(0.05, 0.6, 0.85); // Electric Blue/Cyan
    } else {
        return vec3(0.08, 0.04, 0.18); // Deep Dark Violet
    }
}

// 3: geometric_pulse — Retro cyberpunk expanding neon grid
vec3 bg_geometric_pulse(float u, float v, int ix, int iy, float time) {
    float dx = abs(u - 0.5);
    float dy = abs(v - 0.5);
    float dist = dx + dy; // Diamond distance
    
    // Outer expanding wave pulse
    float pulse = sin(dist * 24.0 - time * 3.5);
    float lineIntensity = smoothstep(0.7, 0.95, pulse);
    
    // Grid alignment
    float grid = abs(fract(dist * 8.0 - time * 1.0) - 0.5);
    float gridLine = smoothstep(0.2, 0.25, grid);
    
    // Mix cyber neon hot pink and deep electric cyan
    vec3 neonColor = mix(vec3(1.0, 0.05, 0.6), vec3(0.05, 0.85, 1.0), sin(time * 0.5 + dist) * 0.5 + 0.5);
    
    if (pulse > 0.4) {
        return mix(vec3(0.06, 0.02, 0.12), neonColor, lineIntensity * 0.7);
    } else if (gridLine < 0.15) {
        return mix(vec3(0.04, 0.02, 0.08), neonColor * 0.4, 1.0 - gridLine / 0.15);
    }
    return vec3(0.04, 0.02, 0.08); // Dark deep space backdrop
}

// 4: plasma_bg — Plasma classique
vec3 bg_plasma(float u, float v, int ix, int iy, float time) {
    float PI = 3.14159265;
    float val = sin(u * 10.0 + time) + sin((v * 10.0 + time) / 2.0)
              + sin((u * 10.0 + v * 10.0 + time) / 2.0);
    float r = sin(val * PI) * 0.5 + 0.5;
    float g = sin(val * PI + 2.0 * PI / 3.0) * 0.5 + 0.5;
    float b = sin(val * PI + 4.0 * PI / 3.0) * 0.5 + 0.5;
    return vec3(r, g, b);
}

// 5: starfield_anim — Champ d'étoiles défilant
vec3 bg_starfield(float u, float v, int ix, int iy, float time) {
    float speed = 15.0;
    float yOffset = float(iy) - time * speed;
    int virtualY = int(floor(yOffset));
    float seed = float(ix) * 12.9898 + float(virtualY) * 78.233;
    float h = fract(abs(sin(seed)) * 43758.5453);
    if (h > 0.97) return vec3(1.0, 1.0, 1.0);
    else if (h > 0.95) return vec3(0.5, 0.8, 1.0);
    else return vec3(0.05, 0.05, 0.1);
}

// 6: cloud_scroll — Nuages défilants
vec3 bg_cloud_scroll(float u, float v, int ix, int iy, float time) {
    float noise = sin((u - time * 0.2) * 10.0) + sin(v * 15.0)
                + cos((u - time * 0.1 - v) * 5.0);
    if (noise > 0.8) return vec3(1.0, 1.0, 1.0);
    else if (noise > 0.4) return vec3(0.85, 0.95, 1.0);
    else return vec3(0.3 + v * 0.3, 0.6 + v * 0.2, 0.9 + v * 0.1);
}

// 7: neon_grid — Grille néon animée
vec3 bg_neon_grid(float u, float v, int ix, int iy, float time) {
    int animY = int(floor(time * 8.0));
    bool onGridX = (ix % 5 == 0);
    bool onGridY = ((iy + animY) % 5 == 0);
    if (onGridX || onGridY) {
        return vec3(0.9 - u * 0.7, 0.1 + u * 0.8, 0.8 + u * 0.2);
    } else {
        return vec3(0.05, 0.02, 0.1);
    }
}

// 8: toxic_sludge — Boue toxique
vec3 bg_toxic_sludge(float u, float v, int ix, int iy, float time) {
    float bubble = sin(u * 12.0 + cos(time * 2.0)) * cos(v * 12.0 - time * 4.0)
                 + sin((u + v) * 8.0);
    if (bubble > 0.8) return vec3(0.8, 1.0, 0.2);
    else if (bubble > 0.2) return vec3(0.3, 0.8, 0.1);
    else if (bubble > -0.5) return vec3(0.1, 0.5, 0.1);
    else return vec3(0.05, 0.2, 0.05);
}

// 9: matrix_code — Code tombant style Matrix
vec3 bg_matrix_code(float u, float v, int ix, int iy, float time) {
    float speed = 20.0;
    float colHash = fract(abs(sin(float(ix) * 12.9898)) * 100.0);
    float yOffset = float(iy) - time * speed + colHash;
    int drop = int(floor(yOffset / 10.0));
    int subY = int(mod(floor(yOffset), 10.0));
    if (subY < 0) subY += 10;
    float cellHash = hash21(float(ix), float(drop));
    if (cellHash > 0.4) {
        if (subY == 9) return vec3(0.8, 1.0, 0.8);
        else if (subY > 5) return vec3(0.2, 0.8, 0.2);
        else if (subY > 2) return vec3(0.05, 0.4, 0.05);
        else return vec3(0.02, 0.05, 0.02);
    }
    return vec3(0.02, 0.05, 0.02);
}

// 10: fire_embers — Braises volantes
vec3 bg_fire_embers(float u, float v, int ix, int iy, float time) {
    float speed = 12.0;
    float sway = sin(float(iy) * 0.1 + time * 2.0) * 3.0;
    float yOffset = float(iy) + time * speed;
    int virtualY = int(floor(yOffset));
    int virtualX = int(floor(float(ix) + sway));
    float h = hash21(float(virtualX), float(virtualY));
    float glow = max(0.0, (v - 0.2) * 1.2);
    if (h > 0.98) return vec3(1.0, 0.9, 0.2);
    else if (h > 0.96) return vec3(1.0, 0.5, 0.1);
    else if (h > 0.94) return vec3(0.8, 0.2, 0.0);
    else return vec3(0.2 + glow * 0.6, 0.05 + glow * 0.2, 0.0);
}

// 11: crystal_cave — Parallax scrolling amethyst facets
vec3 bg_crystal_cave(float u, float v, int ix, int iy, float time) {
    // Parallax layering
    float cx = u * 6.0 + sin(v * 2.0) * 0.3;
    float cy = v * 5.0 - time * 0.15;
    
    float cellX = floor(cx);
    float cellY = floor(cy);
    float lx = cx - cellX;
    float ly = cy - cellY;
    
    float fHash = hash21(cellX, cellY);
    
    // Pointy diamond crystal shard shape
    float shard = abs(lx - 0.5) + ly * 0.5;
    float shine = sin(time * 2.5 + fHash * 15.0) * 0.5 + 0.5;
    
    // Glistening purple/violet mineral palette
    vec3 amethyst = vec3(0.12, 0.04, 0.22); // Crevice dark shadow
    if (shard > 0.8) {
        // Sparkling glistening facet tip
        return mix(vec3(0.5, 0.2, 0.8), vec3(0.95, 0.9, 1.0), shine);
    } else if (shard > 0.4) {
        // Mid-tone sapphire violet reflection
        return mix(vec3(0.3, 0.1, 0.5), vec3(0.2, 0.5, 0.9), shine * 0.4);
    } else if (lx < 0.08 || lx > 0.92 || ly < 0.08 || ly > 0.92) {
        // Shadow crevasses
        return vec3(0.06, 0.02, 0.12);
    }
    return amethyst;
}

// 12: sandstorm — Tempête de sable
vec3 bg_sandstorm(float u, float v, int ix, int iy, float time) {
    float speedX = 30.0;
    float speedY = 10.0;
    float xOffset = float(ix) + time * speedX;
    float yOffset = float(iy) + time * speedY;
    float wave = sin((u * 15.0 + v * 20.0) - time * 8.0);
    int virtualX = int(floor(xOffset));
    int virtualY = int(floor(yOffset));
    float h = hash21(float(virtualX), float(virtualY));
    if (h > 0.85) return vec3(0.95, 0.85, 0.6);
    else if (wave > 0.5) return vec3(0.8, 0.7, 0.4);
    else if (wave > -0.5) return vec3(0.7, 0.6, 0.3);
    else return vec3(0.6, 0.5, 0.2);
}

// 13: aurora_borealis — Aurore boréale
vec3 bg_aurora_borealis(float u, float v, int ix, int iy, float time) {
    float waveBase = sin(u * 8.0 + time * 0.8) * 0.15
                   + sin(u * 3.0 - time * 0.5) * 0.1;
    float band = v + waveBase;
    float ribbon1 = exp(-pow((band - 0.35) * 12.0, 2.0));
    float ribbon2 = exp(-pow((band - 0.55) * 15.0, 2.0));
    float ribbon3 = exp(-pow((band - 0.72) * 10.0, 2.0));
    float shimmer = sin(time * 2.0 + u * 5.0) * 0.2 + 0.8;
    float starSeed = float(ix) * 7.91 + float(iy) * 3.17;
    float starHash = fract(abs(sin(starSeed)) * 43758.5);
    float starGlow = starHash > 0.96 ? (starHash - 0.96) * 25.0 : 0.0;
    float r = 0.02 + ribbon2 * 0.4 * shimmer + ribbon3 * 0.6 + starGlow;
    float g = 0.04 + ribbon1 * 0.9 * shimmer + ribbon2 * 0.3 * shimmer + starGlow;
    float b = 0.08 + ribbon1 * 0.5 + ribbon3 * 0.7 * shimmer + starGlow;
    return vec3(r, g, b);
}

// 14: deep_ocean — Fond abyssal
vec3 bg_deep_ocean(float u, float v, int ix, int iy, float time) {
    float depth = 1.0 - v;
    float baseR = 0.01 + depth * 0.02;
    float baseG = 0.03 + depth * 0.08;
    float baseB = 0.08 + depth * 0.25;
    // Rayons de lumière
    float rayX = u * 6.0;
    int rayCell = int(floor(rayX));
    float rayFrac = rayX - float(rayCell);
    float raySeed = hash11(float(rayCell) * 12.9898);
    float rayIntensity = 0.0;
    if (raySeed > 0.4) {
        float rayWidth = 0.15 + raySeed * 0.1;
        rayIntensity = exp(-pow((rayFrac - 0.5) / rayWidth, 2.0));
        float rayFade = v * (1.0 + sin(time * 0.5 + float(rayCell)));
        rayIntensity *= max(0.0, 1.0 - rayFade * 0.6);
    }
    // Bioluminescence
    float bioSeed = float(ix) * 11.37 + floor(float(iy) + time * 3.0) * 31.41;
    float bioHash = fract(abs(sin(bioSeed)) * 43758.5);
    float bio = bioHash > 0.97 ? (bioHash - 0.97) * 33.0 : 0.0;
    float bioPulse = bio * (sin(time * 4.0 + bioHash * 20.0) * 0.3 + 0.7);
    float r = min(1.0, baseR + rayIntensity * 0.05);
    float g = min(1.0, baseG + rayIntensity * 0.15 + bioPulse * 0.8);
    float b = min(1.0, baseB + rayIntensity * 0.4 + bioPulse * 1.0);
    return vec3(r, g, b);
}

// 15: void_rift — Fissures du vide
vec3 bg_void_rift(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = v - 0.5;
    float dist = sqrt(dx * dx + dy * dy);
    float r = 0.02 + dist * 0.05;
    float g = 0.01;
    float b = 0.05 + dist * 0.1;
    float crack1 = abs(sin(u * 20.0 + v * 15.0 - time * 0.3));
    float crack2 = abs(sin(u * 12.0 - v * 25.0 + time * 0.5));
    float crack3 = abs(sin((u + v) * 18.0 + time * 0.7));
    float rift = pow(max(0.0, 1.0 - crack1 * 8.0), 4.0)
               + pow(max(0.0, 1.0 - crack2 * 8.0), 4.0) * 0.8
               + pow(max(0.0, 1.0 - crack3 * 8.0), 4.0) * 0.6;
    float riftPulse = sin(time * 3.0 + dist * 5.0) * 0.2 + 0.8;
    r = min(1.0, r + rift * 0.9 * riftPulse);
    g = min(1.0, g + rift * 0.1);
    b = min(1.0, b + rift * 1.0 * riftPulse);
    return vec3(r, g, b);
}

// 16: golden_sunset — Coucher de soleil doré
vec3 bg_golden_sunset(float u, float v, int ix, int iy, float time) {
    float skyR = 0.55 + v * 0.45;
    float skyG = 0.15 + v * 0.45;
    float skyB = 0.45 - v * 0.4;
    float sunDx = u - 0.5;
    float sunDy = v - 0.78;
    float sunDist = sqrt(sunDx * sunDx * 1.5 + sunDy * sunDy * 4.0);
    float sunGlow = exp(-sunDist * 8.0);
    float cloud1 = sin(u * 8.0 + time * 0.15) + sin(u * 3.0 - time * 0.1);
    float cloud2 = sin(u * 6.0 - time * 0.2) + sin(u * 4.0 + time * 0.12);
    bool inCloud1 = abs(v - 0.30 + cloud1 * 0.03) < 0.055;
    bool inCloud2 = abs(v - 0.52 + cloud2 * 0.03) < 0.045;
    if (inCloud1 || inCloud2) {
        return vec3(1.0, inCloud1 ? 0.72 : 0.55, inCloud1 ? 0.35 : 0.2);
    }
    return vec3(min(1.0, skyR + sunGlow * 0.6),
                min(1.0, skyG + sunGlow * 0.35),
                min(1.0, skyB + sunGlow * 0.1));
}

// 17: cherry_blossom_wind — Pétales de sakura
vec3 bg_cherry_blossom(float u, float v, int ix, int iy, float time) {
    float r = 0.97 - v * 0.08;
    float g = 0.90 - v * 0.04;
    float b = 0.93 + v * 0.04;
    for (int petal = 0; petal < 10; petal++) {
        float seedX = fract(abs(sin(float(petal) * 37.91)) * 0.9) + 0.05;
        float seedY = fract(abs(sin(float(petal) * 13.37)) * 0.9) + 0.05;
        float seedSpd = 0.4 + abs(sin(float(petal) * 7.13)) * 0.4;
        float px = fract(seedX + time * seedSpd * 0.4);
        float py = fract(seedY + time * seedSpd * 0.25);
        float dist2 = (u - px) * (u - px) * 1600.0 + (v - py) * (v - py) * 900.0;
        if (dist2 < 2.5) {
            r = min(1.0, r * 0.4 + 0.95);
            g = min(1.0, g * 0.4 + 0.55);
            b = min(1.0, b * 0.4 + 0.73);
        }
    }
    return vec3(r, g, b);
}

// 18: cyber_city — Ville cyberpunk
vec3 bg_cyber_city(float u, float v, int ix, int iy, float time) {
    float groundLevel = 0.82;
    float buildHash = fract(abs(sin(floor(u * 10.0) * 12.9898)) * 43758.5);
    float buildHeight = 0.28 + buildHash * 0.42;
    bool inBuilding = v > (groundLevel - buildHeight) && v <= groundLevel;
    if (inBuilding) {
        float r = 0.06, g = 0.06, b = 0.10;
        int winX = int(mod(u * 40.0, 3.0));
        int winY = int(mod(v * 30.0, 3.0));
        if (winX == 1 && winY == 1) {
            float winSeed = floor(u * 40.0 / 3.0) * 7.91 + floor(v * 30.0 / 3.0) * 3.17;
            float winHash = fract(abs(sin(winSeed)) * 43758.5);
            float flicker = sin(time * 0.7 + winHash * 20.0);
            if (winHash > 0.35 && flicker > -0.2) {
                if (winHash > 0.80) { r = 1.0; g = 0.4; b = 0.1; }
                else if (winHash > 0.60) { r = 0.2; g = 0.8; b = 1.0; }
                else { r = 0.8; g = 0.2; b = 1.0; }
            }
        }
        return vec3(r, g, b);
    } else if (v > groundLevel) {
        float reflectFade = max(0.0, 1.0 - (v - groundLevel) * 18.0);
        return vec3(0.03 + reflectFade * 0.2, 0.02 + reflectFade * 0.08, 0.05 + reflectFade * 0.3);
    } else {
        float skyR = 0.02 + v * 0.01;
        float skyG = 0.03 + v * 0.02;
        float skyB = 0.12 + v * 0.05;
        float starSeed = float(ix) * 7.91 + float(iy) * 3.17;
        float starHash = fract(abs(sin(starSeed)) * 43758.5);
        float star = starHash > 0.96 ? (starHash - 0.96) * 25.0 : 0.0;
        return vec3(skyR + star, skyG + star, skyB + star);
    }
}

// 19: ancient_ruins — Ruines antiques avec runes
vec3 bg_ancient_ruins(float u, float v, int ix, int iy, float time) {
    float cellSizeX = 5.0;
    float cellSizeY = 5.0;
    float cellU = u * 40.0 / cellSizeX;
    float cellV = v * 30.0 / cellSizeY;
    int cx = int(floor(cellU));
    int cy = int(floor(cellV));
    float lx = cellU - float(cx);
    float ly = cellV - float(cy);
    float h = fract(abs(sin(float(cx) * 12.9898 + float(cy) * 78.233)) * 43758.5);
    float stoneR = 0.35 + h * 0.12;
    float stoneG = 0.30 + h * 0.10;
    float stoneB = 0.25 + h * 0.08;
    bool isJoint = lx < 0.07 || lx > 0.93 || ly < 0.07 || ly > 0.93;
    if (isJoint) return vec3(0.15, 0.12, 0.10);
    if (h > 0.68) {
        float runeU = (lx - 0.5) * 2.0;
        float runeV = (ly - 0.5) * 2.0;
        bool runePixel = (abs(runeU) < 0.15 && abs(runeV) < 0.8)
                      || (abs(runeV) < 0.15 && abs(runeU) < 0.8)
                      || (abs(abs(runeU) - abs(runeV)) < 0.15 && abs(runeU) > 0.2);
        if (runePixel) {
            float runePulse = sin(time * 2.0 + float(cx) * 1.7 + float(cy) * 2.3) * 0.3 + 0.7;
            if (h > 0.88) return vec3(0.9, 0.6, 0.1) * runePulse;
            else return vec3(0.3, 0.7, 0.9) * runePulse;
        }
    }
    return vec3(stoneR, stoneG, stoneB);
}

// 20: frozen_tundra — Toundra glacée avec blizzard
vec3 bg_frozen_tundra(float u, float v, int ix, int iy, float time) {
    float r = 0.60 + v * 0.25;
    float g = 0.72 + v * 0.18;
    float b = 0.88 + v * 0.10;
    // Traînées de tempête
    float blizzardSpeedX = 28.0;
    float drift = 0.35;
    float sx = u + v * drift - time * blizzardSpeedX / 40.0;
    int virtualSX = int(floor(sx * 40.0));
    for (int streak = 0; streak < 7; streak++) {
        float streakSeed = abs(sin(float(streak) * 17.91)) * 0.5 + 0.5;
        float streakY = streakSeed;
        float streakLen = 0.03 + abs(sin(float(streak) * 7.13)) * 0.05;
        float streakFade = max(0.0, 1.0 - abs(v - streakY) / 0.02);
        if (streakFade > 0.0) {
            float xPhase = mod(float(virtualSX + streak * 37), 40.0) / 40.0;
            float xFade = exp(-pow((xPhase - streakSeed) / streakLen, 2.0));
            float snow = streakFade * xFade;
            r = min(1.0, r + snow * 0.35);
            g = min(1.0, g + snow * 0.35);
            b = min(1.0, b + snow * 0.35);
        }
    }
    // Flocons
    float flakeSeed = float(ix) * 11.37 + floor(float(iy) + time * 6.0) * 31.41;
    float flakeHash = fract(abs(sin(flakeSeed)) * 43758.5);
    if (flakeHash > 0.97) { r = 1.0; g = 1.0; b = 1.0; }
    return vec3(r, g, b);
}

// 21: rainbow_highway — Retro synthwave vaporwave rainbow highway
vec3 bg_rainbow_highway(float u, float v, int ix, int iy, float time) {
    vec3 baseColor = vec3(0.06, 0.02, 0.12);
    
    float perspective = v; 
    float gridX = abs(u - 0.5) / (perspective + 0.05);
    float gridY = 1.0 / (perspective + 0.08) - time * 2.0;
    
    float lineX = abs(fract(gridX * 6.0) - 0.5);
    float lineY = abs(fract(gridY * 4.0) - 0.5);
    
    bool onGrid = (lineX < 0.08) || (lineY < 0.08 && v > 0.1);
    
    float hue = mod(gridY * 0.1 + time * 0.5 + u * 1.5, 3.0);
    vec3 rainbow;
    if (hue < 1.0) {
        rainbow = mix(vec3(1.0, 0.1, 0.5), vec3(0.1, 0.8, 1.0), hue);
    } else if (hue < 2.0) {
        rainbow = mix(vec3(0.1, 0.8, 1.0), vec3(1.0, 0.8, 0.1), hue - 1.0);
    } else {
        rainbow = mix(vec3(1.0, 0.8, 0.1), vec3(1.0, 0.1, 0.5), hue - 2.0);
    }
    
    if (onGrid) {
        return mix(baseColor, rainbow, perspective * 0.9 + 0.1);
    }
    
    float sunY = abs(v - 0.18);
    if (sunY < 0.05) {
        float sunIntensity = 1.0 - sunY / 0.05;
        float stripes = sin(v * 60.0);
        if (stripes > -0.2) {
            return mix(baseColor, vec3(1.0, 0.2, 0.6), sunIntensity * 0.7);
        }
    }
    
    return baseColor;
}

// 22: plasma_storm — Storm clouds with glowing gold/violet lightning bolts
vec3 bg_plasma_storm(float u, float v, int ix, int iy, float time) {
    vec3 baseColor = mix(vec3(0.04, 0.02, 0.08), vec3(0.12, 0.08, 0.22), v);
    
    float flashTrigger = sin(time * 3.5) * cos(time * 1.8 + 2.0);
    bool flash = (flashTrigger > 0.75);
    
    if (flash) {
        baseColor = mix(baseColor, vec3(0.4, 0.2, 0.6), 0.35);
        
        float boltSeed = floor(time * 6.0);
        float boltPath = 0.3 + hash11(boltSeed) * 0.4 + sin(v * 15.0 + time * 10.0) * 0.06;
        float d = abs(u - boltPath);
        
        if (d < 0.015) {
            return vec3(0.95, 0.9, 1.0);
        } else if (d < 0.06) {
            float glow = 1.0 - (d - 0.015) / 0.045;
            return mix(baseColor, vec3(0.9, 0.6, 0.2), glow * 0.85);
        }
    }
    
    float clouds = sin(u * 8.0 - time * 0.4) * cos(v * 6.0 + time * 0.3) * 0.5 + 0.5;
    return mix(baseColor, vec3(0.16, 0.12, 0.28), clouds * 0.3);
}

// 23: galactic_supernova — Starburst with swirling galaxy dust and stellar flares
vec3 bg_galactic_supernova(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = (v - 0.5) * 1.3;
    float r = sqrt(dx * dx + dy * dy);
    float theta = atan(dy, dx);
    
    vec3 baseColor = vec3(0.03, 0.01, 0.07);
    
    float spiral = sin(theta * 3.0 - r * 12.0 + time * 1.5) * 0.5 + 0.5;
    float centerGlow = exp(-pow(r / 0.18, 2.0));
    
    vec3 dustColor = mix(vec3(0.9, 0.1, 0.5), vec3(1.0, 0.7, 0.15), centerGlow);
    
    float raySeed = fract(abs(sin(floor(theta * 10.0) * 123.456)));
    float flares = sin(theta * 20.0 + time * 4.0) * cos(theta * 10.0 - time * 2.0) * 0.5 + 0.5;
    float rayIntensity = flares * exp(-pow(r / 0.45, 2.0)) * (raySeed * 0.5 + 0.5);
    
    vec3 finalDust = mix(baseColor, dustColor, spiral * exp(-pow(r / 0.35, 2.0)) * 0.8 + centerGlow * 0.9);
    return mix(finalDust, vec3(1.0, 0.9, 0.5), rayIntensity * 0.4);
}

// 24: water2 — Stylized Zelda Wind Waker oceanic water with cell-shaded white wave foam
vec3 bg_water2(float u, float v, int ix, int iy, float time) {
    vec3 deepWater = vec3(0.0, 0.42, 0.78);
    vec3 shallowWater = vec3(0.0, 0.65, 0.90);
    vec3 baseColor = mix(shallowWater, deepWater, v);
    
    // Sum of sine/cosine waves moving at different speeds & angles
    float w1 = sin(u * 11.0 + time * 1.4) * 0.45 + cos(v * 9.0 - time * 1.1) * 0.45;
    float w2 = sin((u - v) * 13.0 - time * 1.6) * 0.45 + cos((u + v) * 10.0 + time * 1.3) * 0.45;
    float wave = w1 + w2;
    
    float foamDist = abs(wave - 0.4);
    
    if (foamDist < 0.075) {
        // High-contrast, sharp white cell-shaded foam outlines
        return vec3(0.92, 0.96, 1.0);
    } else if (foamDist < 0.15) {
        // Vibrant cyan foam border glow for depth
        float glow = 1.0 - (foamDist - 0.075) / 0.075;
        return mix(baseColor, vec3(0.3, 0.85, 1.0), glow * 0.7);
    }
    
    // Sparkly peaks
    float sparkSeed = sin(u * 25.0 + time * 3.0) * cos(v * 25.0 - time * 2.5);
    if (sparkSeed > 0.88 && wave > 0.0) {
        return vec3(0.85, 0.95, 1.0);
    }
    
    return baseColor;
}

// 25: mega_energy — Swirling purple and hot pink vortex plasma
vec3 bg_mega_energy(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = (v - 0.5) * 1.3;
    float radius = sqrt(dx * dx + dy * dy);
    float theta = atan(dy, dx);
    
    float swirl = sin(theta * 2.0 - radius * 15.0 + time * 3.5);
    float pulse = sin(radius * 12.0 - time * 2.0);
    float val = swirl * 0.7 + pulse * 0.3;
    
    if (val > 0.3) {
        return vec3(0.95, 0.05, 0.85);
    } else if (val > -0.2) {
        return vec3(0.45, 0.02, 0.75);
    } else {
        return vec3(0.10, 0.01, 0.25);
    }
}

// 26: alola_beach — Turquoise blue waves over golden sand
vec3 bg_alola_beach(float u, float v, int ix, int iy, float time) {
    float wave1 = sin(u * 12.0 - time * 1.5) * 0.04;
    float level = v + wave1;
    
    if (level > 0.78) {
        return vec3(0.94, 0.85, 0.55);
    } else if (level > 0.70) {
        return vec3(0.95, 0.98, 1.0);
    } else if (level > 0.35) {
        return vec3(0.05, 0.75, 0.82);
    } else {
        return vec3(0.35, 0.78, 0.98);
    }
}

// 27: hisui_ancient — Ancient parchment with organic drifting ink wash, double counter-rotating rough Zen calligraphy rings, and floating gold leaf flakes
vec3 bg_hisui_ancient(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = (v - 0.5) * 1.3;
    float radius = sqrt(dx * dx + dy * dy);
    
    // 1. Base parchment color with vignette (darker edges)
    float vignette = clamp(radius * 1.3, 0.0, 1.0);
    vec3 parchmentBase = vec3(0.88, 0.83, 0.72);
    vec3 parchmentEdge = vec3(0.58, 0.48, 0.35);
    vec3 color = mix(parchmentBase, parchmentEdge, vignette);
    
    // 2. Slow drifting organic ink wash / paper noise
    float wash = sin(u * 6.0 + time * 0.2) * cos(v * 4.0 - time * 0.15) * 0.04
               + sin((u - v) * 3.0 + time * 0.08) * 0.02;
    color += vec3(wash * 0.6, wash * 0.4, 0.0);
    
    // 3. Two concentric rough calligraphy rings (enso) that rotate in opposite directions
    // Ring 1 (Outer, rotating CCW)
    float angle1 = atan(dy, dx) - time * 0.6;
    float rough1 = sin(angle1 * 7.0) * 0.02 + cos(angle1 * 13.0) * 0.01;
    float enso1 = abs(radius - 0.24 + rough1);
    float stroke1 = smoothstep(0.035, 0.0, enso1) * (0.35 + 0.65 * (sin(angle1) * 0.5 + 0.5));
    
    // Ring 2 (Inner, rotating CW)
    float angle2 = atan(dy, dx) + time * 0.8;
    float rough2 = cos(angle2 * 6.0) * 0.015 + sin(angle2 * 11.0) * 0.008;
    float enso2 = abs(radius - 0.13 + rough2);
    float stroke2 = smoothstep(0.025, 0.0, enso2) * (0.4 + 0.6 * (cos(angle2) * 0.5 + 0.5));
    
    // Crimson red ink with golden highlight centers
    vec3 crimsonInk = vec3(0.55, 0.12, 0.08);
    vec3 goldHighlight = vec3(0.95, 0.75, 0.2);
    
    if (stroke1 > 0.05) {
        vec3 strokeColor = mix(crimsonInk, goldHighlight, smoothstep(0.015, 0.0, enso1) * 0.5);
        color = mix(color, strokeColor, stroke1 * 0.95);
    }
    if (stroke2 > 0.05) {
        vec3 strokeColor = mix(crimsonInk, goldHighlight, smoothstep(0.01, 0.0, enso2) * 0.5);
        color = mix(color, strokeColor, stroke2 * 0.95);
    }
    
    // 4. Floating ancient gold leaf particles drifting upwards
    float flakeSeed = float(ix) * 19.31 + floor(float(iy) + time * 4.5) * 47.13;
    float flakeHash = fract(abs(sin(flakeSeed)) * 43758.5453);
    if (flakeHash > 0.982 && radius > 0.08) {
        color = vec3(0.92, 0.72, 0.18); // Gold leaf sparkle
    }
    
    return color;
}

// 28: galar_industrial — 3D cylinder pipes with flowing molten heat, circular valve covers, grooves with brass rivets, and dynamic venting steam
vec3 bg_galar_industrial(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.32;
    float dy = v - 0.68;
    
    // 1. Dark industrial metal plating with grooves and rust grunge
    float seamX = abs(fract(u * 3.0) - 0.5);
    float seamY = abs(fract(v * 4.0) - 0.5);
    float seam = (seamX < 0.02 || seamY < 0.02) ? 0.35 : 1.0;
    
    // Plate color with rust/metal textures
    float textureGrunge = sin(u * 25.0) * cos(v * 20.0) * 0.04 
                        + cos(u * 8.0 - v * 15.0) * 0.02;
    vec3 plateBase = vec3(0.15, 0.14, 0.14); // Dark rusted iron
    vec3 color = plateBase * (0.8 + textureGrunge) * seam;
    
    // Rivets on the plate corners
    bool rivet = (ix % 13 == 1 || ix % 13 == 12) && (iy % 10 == 1 || iy % 10 == 9);
    if (rivet && seam > 0.4) {
        color = vec3(0.35, 0.33, 0.30); // Dark brass rivet
    }
    
    // 2. Volumetric Glowing pipes with cylinder shading
    float pipeW = 0.05;
    bool inPipeX = abs(dx) < pipeW;
    bool inPipeY = abs(dy) < pipeW;
    
    vec3 pipeColor = vec3(0.0);
    bool drawPipe = false;
    float shade = 0.0;
    float flow = 0.0;
    
    if (inPipeX || inPipeY) {
        drawPipe = true;
        if (inPipeX && inPipeY) {
            // Junction valve block: circular brass cover
            float d_center = sqrt(dx*dx + dy*dy);
            if (d_center < pipeW) {
                float valShade = sqrt(1.0 - d_center / pipeW);
                pipeColor = vec3(0.8, 0.5, 0.1) * (0.3 + 0.7 * valShade);
                // Pulse center valve light
                float vPulse = sin(time * 5.0) * 0.5 + 0.5;
                if (d_center < 0.015) {
                    pipeColor = vec3(1.0, 0.8, 0.4) * vPulse;
                }
            } else {
                drawPipe = false;
            }
        } else if (inPipeX) {
            float norm = dx / pipeW;
            shade = sqrt(1.0 - norm * norm);
            flow = sin(v * 25.0 - time * 4.0) * 0.3 + 0.7;
            vec3 core = vec3(1.0, 0.45, 0.0) * (0.65 + 0.35 * flow);
            vec3 border = vec3(0.3, 0.04, 0.0);
            pipeColor = mix(border, core, shade);
        } else {
            float norm = dy / pipeW;
            shade = sqrt(1.0 - norm * norm);
            flow = sin(u * 25.0 - time * 4.0) * 0.3 + 0.7;
            vec3 core = vec3(1.0, 0.45, 0.0) * (0.65 + 0.35 * flow);
            vec3 border = vec3(0.3, 0.04, 0.0);
            pipeColor = mix(border, core, shade);
        }
    }
    
    if (drawPipe) {
        color = pipeColor;
    }
    
    // 3. Dynamic Orange Steam rising from the valve / pipes
    float steamDrift = sin(u * 6.0 - time * 1.5) * cos(v * 5.0 - time * 2.0) * 0.5 + 0.5;
    
    // Pressurized steam venting upwards from the horizontal pipe
    if (dy < 0.0) { // Only above the pipe
        float distAbove = abs(dy);
        float spread = distAbove * 0.45;
        // Wavy rising jet path
        float jetPath = dx - sin(v * 16.0 - time * 6.0) * 0.04;
        float jet = smoothstep(0.08 + spread, 0.0, abs(jetPath)) 
                  * smoothstep(0.45, 0.0, distAbove);
                  
        // Inject glowing steam color
        color += vec3(0.95, 0.4, 0.05) * jet * (0.4 + 0.6 * steamDrift);
    }
    
    // 4. Glowing furnace glow from the bottom
    float furnace = (0.5 + 0.5 * sin(time * 2.0)) * (v * v * 0.22);
    color += vec3(0.3, 0.08, 0.0) * furnace;
    
    return color;
}

// 29: paldea_crystal — Terastal color-shifting crystal facets
vec3 bg_paldea_crystal(float u, float v, int ix, int iy, float time) {
    float dist = sqrt((u - 0.5) * (u - 0.5) + (v - 0.5) * (v - 0.5));
    float angle = atan(v - 0.5, u - 0.5);
    float val = sin(angle * 6.0 + time * 2.0) * 0.2 + sin(dist * 20.0 - time * 3.0) * 0.3;
    
    float hue = mod(u + v + val + time * 0.5, 3.0);
    if (hue < 0.0) hue += 3.0;
    
    float r = 0.0;
    float g = 0.0;
    float b = 0.0;
    
    if (hue < 1.0) {
        r = 0.9; g = 0.4 + hue * 0.5; b = 0.9;
    } else if (hue < 2.0) {
        float h2 = hue - 1.0;
        r = 0.9 - h2 * 0.5; g = 0.9; b = 0.4 + h2 * 0.5;
    } else {
        float h3 = hue - 2.0;
        r = 0.4 + h3 * 0.5; g = 0.9 - h3 * 0.5; b = 0.9;
    }
    
    float flash = sin(u * 50.0 + time * 5.0) * cos(v * 50.0 - time * 5.0);
    if (flash > 0.8) {
        r = 1.0; g = 1.0; b = 1.0;
    }
    return vec3(r, g, b);
}

// 30: distortion_rift — Swirling purple/lime-green antimatter plasma nebula with floating geometric dark iron plates
vec3 bg_distortion_rift(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = (v - 0.5) * 1.3;
    float radius = sqrt(dx * dx + dy * dy);
    float theta = atan(dy, dx);
    
    // Swirling purple (0.5, 0.05, 0.7) and lime green (0.35, 0.85, 0.1)
    float swirl = sin(theta * 3.0 - radius * 12.0 + time * 2.0);
    float pulse = cos(radius * 8.0 - time * 1.5);
    float val = swirl * 0.6 + pulse * 0.4;
    
    vec3 baseColor = vec3(0.06, 0.01, 0.12);
    vec3 purpleNebula = vec3(0.5, 0.05, 0.7);
    vec3 limeNebula = vec3(0.35, 0.85, 0.1);
    
    vec3 color = mix(baseColor, purpleNebula, clamp((val + 0.5) * 0.8, 0.0, 1.0));
    color = mix(color, limeNebula, clamp((swirl - 0.3) * 0.7, 0.0, 1.0));
    
    // Floating plates
    float pSpeed = 0.06;
    for (int p = 0; p < 5; p++) {
        float seed = float(p) * 17.54;
        float pX = 0.15 + fract(sin(seed) * 100.0) * 0.7;
        float pY = mod(0.1 + fract(cos(seed) * 100.0) + time * pSpeed * (1.0 + float(p) * 0.2), 1.2) - 0.1;
        float w = 0.08 + fract(sin(seed + 1.0)) * 0.06;
        float h = 0.05 + fract(cos(seed + 2.0)) * 0.04;
        pY += sin(time * 2.0 + seed) * 0.02;
        
        if (abs(u - pX) < w && abs(v - pY) < h) {
            float edgeDist = min(w - abs(u - pX), h - abs(v - pY));
            if (edgeDist < 0.012) {
                color = vec3(0.12, 0.1, 0.16);
            } else {
                color = vec3(0.04, 0.03, 0.06);
            }
            break;
        }
    }
    return color;
}

// 31: dreamscape — Soft pulsing pastel pink/violet/cyan nebula with glowing translucent dream bubbles
vec3 bg_dreamscape(float u, float v, int ix, int iy, float time) {
    float dx = u - 0.5;
    float dy = (v - 0.5) * 1.3;
    float radius = sqrt(dx * dx + dy * dy);
    
    float drift = sin(u * 4.0 - time * 0.5) * cos(v * 4.0 + time * 0.4) * 0.5 + 0.5;
    vec3 pastelPink = vec3(0.98, 0.65, 0.75);
    vec3 pastelPurple = vec3(0.75, 0.58, 0.95);
    vec3 pastelCyan = vec3(0.55, 0.88, 0.92);
    
    vec3 color = mix(pastelPink, pastelPurple, drift);
    color = mix(color, pastelCyan, radius * 0.8);
    
    float bSpeed = 0.07;
    for (int b = 0; b < 4; b++) {
        float seed = float(b) * 23.87;
        float bX = 0.2 + fract(sin(seed) * 100.0) * 0.6 + sin(time * 0.8 + seed) * 0.04;
        float bY = mod(1.1 - time * bSpeed - fract(cos(seed) * 100.0), 1.3) - 0.15;
        float rad = 0.07 + fract(sin(seed * 3.0)) * 0.05;
        float dist = sqrt((u - bX)*(u - bX) + (v - bY)*(v - bY) * 1.69);
        
        if (dist < rad) {
            float edge = dist / rad;
            if (edge > 0.85) {
                color = mix(color, vec3(1.0, 1.0, 1.0), 0.75 * (edge - 0.85) / 0.15);
            } else {
                color += vec3(0.08, 0.04, 0.12) * (1.0 - edge);
            }
        }
    }
    return color;
}

// 32: magma_chamber — Molten gold and deep vermilion lava currents with rising heat embers
vec3 bg_magma_chamber(float u, float v, int ix, int iy, float time) {
    float wave1 = sin(u * 8.0 - time * 1.5) * 0.15;
    float wave2 = cos(v * 10.0 + time * 1.2) * 0.1;
    float wave = wave1 + wave2;
    
    vec3 deepMagma = vec3(0.35, 0.04, 0.02);
    vec3 brightLava = vec3(0.95, 0.25, 0.05);
    vec3 liquidGold = vec3(1.0, 0.85, 0.1);
    
    float val = sin((u + v) * 6.0 + wave - time * 0.8) * 0.5 + 0.5;
    vec3 color = mix(deepMagma, brightLava, val);
    float goldVeins = smoothstep(0.72, 0.95, val);
    color = mix(color, liquidGold, goldVeins * 0.8);
    
    float emberSeed = float(ix) * 23.31 + floor(float(iy) + time * 12.0) * 53.41;
    float emberHash = fract(abs(sin(emberSeed)) * 43758.5453);
    if (emberHash > 0.978) {
        color = mix(color, vec3(1.0, 0.75, 0.2), 0.75);
    }
    return color;
}

// ===== MAIN =====

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

    vec3 color = vec3(0.0);

    // Dispatch vers l'effet approprié
    if      (effectId == 0)  color = bg_water_anim(u, v, ix, iy, time);
    else if (effectId == 1)  color = bg_lava_anim(u, v, ix, iy, time);
    else if (effectId == 2)  color = bg_balatro_swirl(u, v, ix, iy, time);
    else if (effectId == 3)  color = bg_geometric_pulse(u, v, ix, iy, time);
    else if (effectId == 4)  color = bg_plasma(u, v, ix, iy, time);
    else if (effectId == 5)  color = bg_starfield(u, v, ix, iy, time);
    else if (effectId == 6)  color = bg_cloud_scroll(u, v, ix, iy, time);
    else if (effectId == 7)  color = bg_neon_grid(u, v, ix, iy, time);
    else if (effectId == 8)  color = bg_toxic_sludge(u, v, ix, iy, time);
    else if (effectId == 9)  color = bg_matrix_code(u, v, ix, iy, time);
    else if (effectId == 10) color = bg_fire_embers(u, v, ix, iy, time);
    else if (effectId == 11) color = bg_crystal_cave(u, v, ix, iy, time);
    else if (effectId == 12) color = bg_sandstorm(u, v, ix, iy, time);
    else if (effectId == 13) color = bg_aurora_borealis(u, v, ix, iy, time);
    else if (effectId == 14) color = bg_deep_ocean(u, v, ix, iy, time);
    else if (effectId == 15) color = bg_void_rift(u, v, ix, iy, time);
    else if (effectId == 16) color = bg_golden_sunset(u, v, ix, iy, time);
    else if (effectId == 17) color = bg_cherry_blossom(u, v, ix, iy, time);
    else if (effectId == 18) color = bg_cyber_city(u, v, ix, iy, time);
    else if (effectId == 19) color = bg_ancient_ruins(u, v, ix, iy, time);
    else if (effectId == 20) color = bg_frozen_tundra(u, v, ix, iy, time);
    else if (effectId == 21) color = bg_rainbow_highway(u, v, ix, iy, time);
    else if (effectId == 22) color = bg_plasma_storm(u, v, ix, iy, time);
    else if (effectId == 23) color = bg_galactic_supernova(u, v, ix, iy, time);
    else if (effectId == 24) color = bg_water2(u, v, ix, iy, time);
    else if (effectId == 25) color = bg_mega_energy(u, v, ix, iy, time);
    else if (effectId == 26) color = bg_alola_beach(u, v, ix, iy, time);
    else if (effectId == 27) color = bg_hisui_ancient(u, v, ix, iy, time);
    else if (effectId == 28) color = bg_galar_industrial(u, v, ix, iy, time);
    else if (effectId == 29) color = bg_paldea_crystal(u, v, ix, iy, time);
    else if (effectId == 30) color = bg_distortion_rift(u, v, ix, iy, time);
    else if (effectId == 31) color = bg_dreamscape(u, v, ix, iy, time);
    else if (effectId == 32) color = bg_magma_chamber(u, v, ix, iy, time);

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}

package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class OrnamentalPatternStyle(
    val displayName: String,
    val description: String
) {
    NONE("Clean Minimal", "Clean surface without ornamental textures"),
    AROMATIC_LAVENDER("Provence Lavender & Botanical", "Delicate lavender sprigs, blooming buds and floral vine filigree"),
    SACRED_MANDALA("Sacred Mandala & Oud Arabesque", "Intricate 12-fold geometric mandala lace, incense wisps and arabesque filigree"),
    BAROQUE_JASMINE("Baroque Jasmine Lace", "Delicate 5-petal star jasmine blossoms and royal French lace flourishes"),
    VICTORIAN_DAMASK("Victorian Damask Crests", "Royal tapestry damask fleurons, acanthus leaf scrolls and ornate heart curls"),
    BYZANTINE_GOLD("Byzantine Golden Arabesque", "Majestic 8-point Islamic sacred stars, incense smoke coils and golden filigree"),
    MEDITERRANEAN_NEROLI("Mediterranean Majolica Tiles", "Sunlit citrus blossom wreaths, ornate ceramic tile corners and sunbursts"),
    FESTIVE_RIBBON("Festive Gift Ribbons & Sparkles", "Gift box satin diagonal ribbon sash, bow knots and holiday twinkle stars"),
    SAKURA_PETALS("Sakura Blossom Petals", "Cascading cherry blossoms with gentle organic wind trails"),
    ROYAL_FLEUR_DE_LIS("Imperial Fleur-de-Lis", "Regal French monarchy heraldic fleurons, crown arches and imperial emblems"),
    CYBER_CIRCUITS("Cyber Matrix Circuit Lattice", "Futuristic printed circuit nodes, hex grid corners and laser traces"),
    CONSTELLATION_NEBULA("Celestial Nebula & Constellations", "Starry night galaxy coordinates, cosmic stardust and constellation charts"),
    MINIMALIST_AURA("Ambient Aura Glow", "Soft luminous radial aura glow"),
    SPATIAL_ORBITAL_DEPTH("Spatial 3D Orbital Rings", "Floating 3D depth rings, orbital halo horizons and luminous stardust"),
    SPATIAL_HOLOGRAPHIC_GRID("Spatial Hologram Depth Grid", "Atmospheric perspective depth grid, floating laser nodes and neon horizon"),
    SPATIAL_AURORA_PORTAL("Spatial Aurora Refraction", "Vision frosted glass refraction rays with chromatic chromatic aberration")
}

enum class AppThemeSkin(
    val displayName: String,
    val categoryTag: String,
    val description: String,
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfacePressed: Color,
    val outline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val defaultAccent: Color,
    val previewColors: List<Color>,
    val badgeLabel: String,
    val ornamentalPattern: OrnamentalPatternStyle = OrnamentalPatternStyle.NONE,
    val ornamentalTintColor: Color = Color.Transparent
) {
    // ==========================================
    // THEMES: SUPER AMOLED & PURE LIGHT
    // ==========================================
    DARK_CHARCOAL(
        displayName = "Super AMOLED",
        categoryTag = "Super AMOLED",
        description = "Pure black OLED background (#000000) with tactile true black surfaces, luminous orange operator keys & high-contrast crisp text",
        isDark = true,
        background = Color(0xFF000000),
        surface = Color(0xFF000000),
        surfaceVariant = Color(0xFF0A0A0A),
        surfacePressed = Color(0xFF181818),
        outline = Color(0x38FFFFFF),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA1A1AA),
        defaultAccent = Color(0xFFFF9F0A),
        previewColors = listOf(Color(0xFF000000), Color(0xFF0A0A0A), Color(0xFFFF9F0A)),
        badgeLabel = "SUPER AMOLED",
        ornamentalPattern = OrnamentalPatternStyle.NONE,
        ornamentalTintColor = Color.Transparent
    ),
    PURE_LIGHT(
        displayName = "Pure Light",
        categoryTag = "Light Theme",
        description = "Pristine white canvas with crisp tactile keys, radiant orange operators & soft silver action keys",
        isDark = false,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF2F2F7),
        surfacePressed = Color(0xFFE5E5EA),
        outline = Color(0x1F000000),
        textPrimary = Color(0xFF1C1C1E),
        textSecondary = Color(0xFF6E6E73),
        defaultAccent = Color(0xFFFF9F0A),
        previewColors = listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7), Color(0xFFFF9F0A)),
        badgeLabel = "PURE LIGHT",
        ornamentalPattern = OrnamentalPatternStyle.NONE,
        ornamentalTintColor = Color.Transparent
    ),

    // ==========================================
    // 0. SPATIAL COMPUTING & VISION OS MASTERPIECES
    // ==========================================
    SPATIAL_VISION_DARK(
        displayName = "Spatial Frosted Glass (Vision Dark)",
        categoryTag = "Spatial UI",
        description = "Vision-inspired frosted acrylic glass with 3D specular edge highlights, floating depth & luminous cyan glow",
        isDark = true,
        background = Color(0xFF070B14),
        surface = Color(0xFF0E1626),
        surfaceVariant = Color(0x2E38BDF8),
        surfacePressed = Color(0x5438BDF8),
        outline = Color(0x4D38BDF8),
        textPrimary = Color(0xFFF0F9FF),
        textSecondary = Color(0xFFBAE6FD),
        defaultAccent = Color(0xFF00E5FF),
        previewColors = listOf(Color(0xFF070B14), Color(0xFF00E5FF), Color(0xFF818CF8)),
        badgeLabel = "SPATIAL GLASS",
        ornamentalPattern = OrnamentalPatternStyle.SPATIAL_ORBITAL_DEPTH,
        ornamentalTintColor = Color(0xFF00E5FF)
    ),
    SPATIAL_NEBULA_HOLO(
        displayName = "Spatial Nebula & Holo Matrix",
        categoryTag = "Spatial UI",
        description = "Deep space horizon grid with floating holographic coordinates & radiant ultraviolet aura",
        isDark = true,
        background = Color(0xFF080616),
        surface = Color(0xFF140F2D),
        surfaceVariant = Color(0x33A855F7),
        surfacePressed = Color(0x55A855F7),
        outline = Color(0x59A855F7),
        textPrimary = Color(0xFFFAF5FF),
        textSecondary = Color(0xFFE9D5FF),
        defaultAccent = Color(0xFFA855F7),
        previewColors = listOf(Color(0xFF080616), Color(0xFFA855F7), Color(0xFF00F0FF)),
        badgeLabel = "SPATIAL HOLO",
        ornamentalPattern = OrnamentalPatternStyle.SPATIAL_HOLOGRAPHIC_GRID,
        ornamentalTintColor = Color(0xFFA855F7)
    ),
    SPATIAL_CRYSTAL_LIGHT(
        displayName = "Spatial Crystalline (Vision Light)",
        categoryTag = "Spatial UI",
        description = "Pure crystalline frosted glass with specular top refraction, diamond clarity & vivid sky blue accents",
        isDark = false,
        background = Color(0xFFF1F5F9),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0x80FFFFFF),
        surfacePressed = Color(0xB3E2E8F0),
        outline = Color(0x4D0EA5E9),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF334155),
        defaultAccent = Color(0xFF0EA5E9),
        previewColors = listOf(Color(0xFFFFFFFF), Color(0xFFE0F2FE), Color(0xFF0EA5E9)),
        badgeLabel = "SPATIAL CRYSTAL",
        ornamentalPattern = OrnamentalPatternStyle.SPATIAL_AURORA_PORTAL,
        ornamentalTintColor = Color(0xFF0EA5E9)
    ),
    // ==========================================
    // 1. AROMATIC & ORNAMENTAL MASTERPIECES
    // ==========================================
    AROMATIC_LAVENDER(
        displayName = "Aromatic Lavender Botanical",
        categoryTag = "Aromatic",
        description = "French Provence lavender sprigs, calming lilac mist & ornamental botanical vine filigree backdrop",
        isDark = true,
        background = Color(0xFF130B21),
        surface = Color(0xFF1F1235),
        surfaceVariant = Color(0x33C084FC),
        surfacePressed = Color(0x55C084FC),
        outline = Color(0x55C084FC),
        textPrimary = Color(0xFFFAF5FF),
        textSecondary = Color(0xFFE9D5FF),
        defaultAccent = Color(0xFFC084FC),
        previewColors = listOf(Color(0xFF130B21), Color(0xFF9333EA), Color(0xFFC084FC)),
        badgeLabel = "AROMATIC VINES",
        ornamentalPattern = OrnamentalPatternStyle.AROMATIC_LAVENDER,
        ornamentalTintColor = Color(0xFFC084FC)
    ),
    AROMATIC_SANDALWOOD_OUD(
        displayName = "Aromatic Sandalwood & Oud",
        categoryTag = "Aromatic",
        description = "Sacred Mysore sandalwood incense, aged royal oud resin & intricate ornamental mandala lace backdrop",
        isDark = true,
        background = Color(0xFF170C06),
        surface = Color(0xFF27150A),
        surfaceVariant = Color(0x33F59E0B),
        surfacePressed = Color(0x55F59E0B),
        outline = Color(0x55F59E0B),
        textPrimary = Color(0xFFFFFBEB),
        textSecondary = Color(0xFFFDE68A),
        defaultAccent = Color(0xFFF59E0B),
        previewColors = listOf(Color(0xFF170C06), Color(0xFFB45309), Color(0xFFF59E0B)),
        badgeLabel = "SACRED MANDALA",
        ornamentalPattern = OrnamentalPatternStyle.SACRED_MANDALA,
        ornamentalTintColor = Color(0xFFF59E0B)
    ),
    AROMATIC_JASMINE_TEA(
        displayName = "Aromatic Jasmine & White Tea",
        categoryTag = "Aromatic",
        description = "Luminous star jasmine petals, fragrant imperial white tea & delicate baroque floral lace background",
        isDark = false,
        background = Color(0xFFFAF8F5),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0x99F1EADF),
        surfacePressed = Color(0xBFE7DCCC),
        outline = Color(0x55D97706),
        textPrimary = Color(0xFF292524),
        textSecondary = Color(0xFF57534E),
        defaultAccent = Color(0xFFD97706),
        previewColors = listOf(Color(0xFFFAF8F5), Color(0xFFFDE047), Color(0xFFD97706)),
        badgeLabel = "JASMINE LACE",
        ornamentalPattern = OrnamentalPatternStyle.BAROQUE_JASMINE,
        ornamentalTintColor = Color(0xFFD97706)
    ),
    AROMATIC_ROSE_DAMASK(
        displayName = "Aromatic Rose Oud & Damask",
        categoryTag = "Aromatic",
        description = "Persian Ispahan damask rose, precious rosewood & regal ornamental Victorian damask crests",
        isDark = true,
        background = Color(0xFF1D0813),
        surface = Color(0xFF2E0D1F),
        surfaceVariant = Color(0x38F43F5E),
        surfacePressed = Color(0x59F43F5E),
        outline = Color(0x59F43F5E),
        textPrimary = Color(0xFFFFF1F2),
        textSecondary = Color(0xFFFECDD3),
        defaultAccent = Color(0xFFF43F5E),
        previewColors = listOf(Color(0xFF1D0813), Color(0xFFBE123C), Color(0xFFF43F5E)),
        badgeLabel = "ROYAL DAMASK",
        ornamentalPattern = OrnamentalPatternStyle.VICTORIAN_DAMASK,
        ornamentalTintColor = Color(0xFFF43F5E)
    ),
    AROMATIC_FRANKINCENSE_MYRRH(
        displayName = "Aromatic Frankincense & Gold",
        categoryTag = "Aromatic",
        description = "Sacred Arabian frankincense, golden myrrh resin & majestic Byzantine arabesque ornamental stars",
        isDark = true,
        background = Color(0xFF140D04),
        surface = Color(0xFF231707),
        surfaceVariant = Color(0x33FFD700),
        surfacePressed = Color(0x55FFD700),
        outline = Color(0x66FFD700),
        textPrimary = Color(0xFFFFFBEB),
        textSecondary = Color(0xFFFFE082),
        defaultAccent = Color(0xFFFFD700),
        previewColors = listOf(Color(0xFF140D04), Color(0xFFCA8A04), Color(0xFFFFD700)),
        badgeLabel = "BYZANTINE GOLD",
        ornamentalPattern = OrnamentalPatternStyle.BYZANTINE_GOLD,
        ornamentalTintColor = Color(0xFFFFD700)
    ),
    AROMATIC_NEROLI_BLOSSOM(
        displayName = "Aromatic Neroli Citrus Blossom",
        categoryTag = "Aromatic",
        description = "Sunlit Sicilian orange blossom, fresh neroli essence & Mediterranean ornate majolica ceramic tile art",
        isDark = false,
        background = Color(0xFFFFFDF8),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0x99FFE8D6),
        surfacePressed = Color(0xBFFFD0B5),
        outline = Color(0x55EA580C),
        textPrimary = Color(0xFF1C1917),
        textSecondary = Color(0xFF44403C),
        defaultAccent = Color(0xFFEA580C),
        previewColors = listOf(Color(0xFFFFFDF8), Color(0xFF38BDF8), Color(0xFFEA580C)),
        badgeLabel = "NEROLI TILES",
        ornamentalPattern = OrnamentalPatternStyle.MEDITERRANEAN_NEROLI,
        ornamentalTintColor = Color(0xFFEA580C)
    ),

    // ==========================================
    // 2. FESTIVE GIFT WRAP & HOLIDAY CELEBRATION
    // ==========================================
    GIFT_CELEBRATION(
        displayName = "Festive Gift Box & Ribbon",
        categoryTag = "Gift Style",
        description = "Celebratory gift wrap with rich ruby velvet, gold foil ribbon accents & festive holiday sparkle",
        isDark = true,
        background = Color(0xFF1E0A12),
        surface = Color(0xFF2E101D),
        surfaceVariant = Color(0x38FF2A55),
        surfacePressed = Color(0x55FF2A55),
        outline = Color(0x66FFD700),
        textPrimary = Color(0xFFFFF7EB),
        textSecondary = Color(0xFFFFD180),
        defaultAccent = Color(0xFFFF2A55),
        previewColors = listOf(Color(0xFFFF2A55), Color(0xFFFFD700), Color(0xFF1E0A12)),
        badgeLabel = "GIFT STYLE",
        ornamentalPattern = OrnamentalPatternStyle.FESTIVE_RIBBON,
        ornamentalTintColor = Color(0xFFFFD700)
    ),

    // ==========================================
    // 3. CLASSIC & MODERN DARK SKINS
    // ==========================================
    DARK_OBSIDIAN(
        displayName = "Obsidian Liquid Dark",
        categoryTag = "Dark Mode",
        description = "Classic deep midnight obsidian glass with subtle starry constellations and luminous cyan accents",
        isDark = true,
        background = Color(0xFF0A0E17),
        surface = Color(0xFF0F172A),
        surfaceVariant = Color(0x1AFFFFFF),
        surfacePressed = Color(0x33FFFFFF),
        outline = Color(0x2EFFFFFF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        defaultAccent = Color(0xFF00E5FF),
        previewColors = listOf(Color(0xFF0A0E17), Color(0xFF1E293B), Color(0xFF00E5FF)),
        badgeLabel = "CLASSIC DARK",
        ornamentalPattern = OrnamentalPatternStyle.CONSTELLATION_NEBULA,
        ornamentalTintColor = Color(0xFF00E5FF)
    ),
    OLED_TRUE_BLACK(
        displayName = "OLED True Pitch Black",
        categoryTag = "Dark Mode",
        description = "Pure 100% true black canvas with high-efficiency luminescent edges and battery saving",
        isDark = true,
        background = Color(0xFF000000),
        surface = Color(0xFF080808),
        surfaceVariant = Color(0x1FFFFFFF),
        surfacePressed = Color(0x3BFFFFFF),
        outline = Color(0x38FFFFFF),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA1A1AA),
        defaultAccent = Color(0xFFF4F4F5),
        previewColors = listOf(Color(0xFF000000), Color(0xFF18181B), Color(0xFFFFFFFF)),
        badgeLabel = "OLED BLACK",
        ornamentalPattern = OrnamentalPatternStyle.NONE
    ),

    // ==========================================
    // 4. CLASSIC & MODERN LIGHT SKINS
    // ==========================================
    LIGHT_PORCELAIN(
        displayName = "Porcelain Crystal Light",
        categoryTag = "Light Mode",
        description = "Ultra-clean alabaster porcelain with crystalline glass reflections and crisp legibility",
        isDark = false,
        background = Color(0xFFF1F5F9),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0x80FFFFFF),
        surfacePressed = Color(0xB3FFFFFF),
        outline = Color(0x4D000000),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF475569),
        defaultAccent = Color(0xFF0091EA),
        previewColors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF0284C7)),
        badgeLabel = "CLASSIC LIGHT",
        ornamentalPattern = OrnamentalPatternStyle.MINIMALIST_AURA,
        ornamentalTintColor = Color(0xFF0284C7)
    ),

    // ==========================================
    // 5. ARTISTIC & CREATIVE DESIGN
    // ==========================================
    ARTISTIC_STUDIO(
        displayName = "Artistic Studio Color Design",
        categoryTag = "Color Design",
        description = "Contemporary Bauhaus creative design studio with vibrant pop-art color accents",
        isDark = false,
        background = Color(0xFFF8FAFC),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0x52E2E8F0),
        surfacePressed = Color(0x80CBD5E1),
        outline = Color(0x4D2563EB),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF334155),
        defaultAccent = Color(0xFF2563EB),
        previewColors = listOf(Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF2563EB)),
        badgeLabel = "COLOR DESIGN",
        ornamentalPattern = OrnamentalPatternStyle.MINIMALIST_AURA,
        ornamentalTintColor = Color(0xFF2563EB)
    ),

    // ==========================================
    // 6. SCI-FI & CYBERPUNK
    // ==========================================
    CYBER_MATRIX(
        displayName = "Cyberpunk Neon Matrix",
        categoryTag = "Sci-Fi",
        description = "Electric hacker terminal with glowing neon emerald grid & synthwave cyan highlights",
        isDark = true,
        background = Color(0xFF05120B),
        surface = Color(0xFF0A2014),
        surfaceVariant = Color(0x2E00E676),
        surfacePressed = Color(0x4D00E676),
        outline = Color(0x5900E676),
        textPrimary = Color(0xFFE0FFE9),
        textSecondary = Color(0xFF6EE7B7),
        defaultAccent = Color(0xFF00E676),
        previewColors = listOf(Color(0xFF05120B), Color(0xFF00E676), Color(0xFF00E5FF)),
        badgeLabel = "CYBER MATRIX",
        ornamentalPattern = OrnamentalPatternStyle.CYBER_CIRCUITS,
        ornamentalTintColor = Color(0xFF00E676)
    ),
    RETRO_SYNTHWAVE(
        displayName = "Retro Synthwave 80s",
        categoryTag = "Retro",
        description = "Nostalgic 1980s neon synthwave with chromatic magenta, laser cyan and twilight purple",
        isDark = true,
        background = Color(0xFF150024),
        surface = Color(0xFF25003E),
        surfaceVariant = Color(0x38FF007F),
        surfacePressed = Color(0x59FF007F),
        outline = Color(0x5500F0FF),
        textPrimary = Color(0xFFFFF0FA),
        textSecondary = Color(0xFFFF80DF),
        defaultAccent = Color(0xFFFF007F),
        previewColors = listOf(Color(0xFFFF007F), Color(0xFF00F0FF), Color(0xFF150024)),
        badgeLabel = "SYNTHWAVE 80S",
        ornamentalPattern = OrnamentalPatternStyle.CYBER_CIRCUITS,
        ornamentalTintColor = Color(0xFFFF007F)
    ),

    // ==========================================
    // 7. PASTEL & ROMANTIC
    // ==========================================
    CHERRY_BLOSSOM(
        displayName = "Cherry Blossom Pastel",
        categoryTag = "Pastel",
        description = "Soft delicate Japanese Sakura pink with dreamy pastel frosted milk glass & falling petals",
        isDark = false,
        background = Color(0xFFFFF0F5),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0x99FFE4E6),
        surfacePressed = Color(0xBFFECDD3),
        outline = Color(0x4DF43F5E),
        textPrimary = Color(0xFF4C0519),
        textSecondary = Color(0xFF9F1239),
        defaultAccent = Color(0xFFFB7185),
        previewColors = listOf(Color(0xFFFFF0F5), Color(0xFFFDA4AF), Color(0xFFFB7185)),
        badgeLabel = "PASTEL BLOSSOM",
        ornamentalPattern = OrnamentalPatternStyle.SAKURA_PETALS,
        ornamentalTintColor = Color(0xFFFB7185)
    ),
    ROSE_GOLD_LUXURY(
        displayName = "Rose Gold Champagne",
        categoryTag = "Luxury",
        description = "Sophisticated champagne rose gold metal with sparkling jewelry gloss and warm bronze",
        isDark = true,
        background = Color(0xFF190F14),
        surface = Color(0xFF2B1922),
        surfaceVariant = Color(0x33FB7185),
        surfacePressed = Color(0x52FB7185),
        outline = Color(0x59FB7185),
        textPrimary = Color(0xFFFFF1F2),
        textSecondary = Color(0xFFFECDD3),
        defaultAccent = Color(0xFFFB7185),
        previewColors = listOf(Color(0xFF190F14), Color(0xFFBE123C), Color(0xFFFDA4AF)),
        badgeLabel = "ROSE GOLD",
        ornamentalPattern = OrnamentalPatternStyle.VICTORIAN_DAMASK,
        ornamentalTintColor = Color(0xFFFB7185)
    ),

    // ==========================================
    // 8. ROYAL & IMPERIAL
    // ==========================================
    ROYAL_AMETHYST(
        displayName = "Royal Amethyst Velvet",
        categoryTag = "Royalty",
        description = "Deep majestic imperial purple with glowing ultraviolet glass crystals and gold fleur-de-lis",
        isDark = true,
        background = Color(0xFF13091F),
        surface = Color(0xFF200F35),
        surfaceVariant = Color(0x33D500F9),
        surfacePressed = Color(0x54D500F9),
        outline = Color(0x59D500F9),
        textPrimary = Color(0xFFFDF4FF),
        textSecondary = Color(0xFFD8B4FE),
        defaultAccent = Color(0xFFD500F9),
        previewColors = listOf(Color(0xFF13091F), Color(0xFF7E22CE), Color(0xFFD500F9)),
        badgeLabel = "ROYAL AMETHYST",
        ornamentalPattern = OrnamentalPatternStyle.ROYAL_FLEUR_DE_LIS,
        ornamentalTintColor = Color(0xFFFFD700)
    ),

    // ==========================================
    // 9. NATURE & ORGANIC
    // ==========================================
    EMERALD_FOREST(
        displayName = "Nordic Emerald Forest",
        categoryTag = "Nature",
        description = "Enchanting deep pine forest with soothing misty jade crystalline surfaces",
        isDark = true,
        background = Color(0xFF061712),
        surface = Color(0xFF0D281E),
        surfaceVariant = Color(0x3310B981),
        surfacePressed = Color(0x5210B981),
        outline = Color(0x4D10B981),
        textPrimary = Color(0xFFECFDF5),
        textSecondary = Color(0xFFA7F3D0),
        defaultAccent = Color(0xFF10B981),
        previewColors = listOf(Color(0xFF061712), Color(0xFF047857), Color(0xFF34D399)),
        badgeLabel = "NORDIC FOREST",
        ornamentalPattern = OrnamentalPatternStyle.AROMATIC_LAVENDER,
        ornamentalTintColor = Color(0xFF34D399)
    ),
    MATCHA_ZEN(
        displayName = "Zen Matcha Garden",
        categoryTag = "Nature",
        description = "Harmonious Japanese Kyoto green tea with calming bamboo minimalist aesthetic",
        isDark = false,
        background = Color(0xFFF4F7F2),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0x99E2EBDD),
        surfacePressed = Color(0xBFD1DFC8),
        outline = Color(0x4D65A30D),
        textPrimary = Color(0xFF1E2D18),
        textSecondary = Color(0xFF455A38),
        defaultAccent = Color(0xFF65A30D),
        previewColors = listOf(Color(0xFFF4F7F2), Color(0xFFA3E635), Color(0xFF4D7C0F)),
        badgeLabel = "ZEN MATCHA",
        ornamentalPattern = OrnamentalPatternStyle.BAROQUE_JASMINE,
        ornamentalTintColor = Color(0xFF65A30D)
    ),

    // ==========================================
    // 10. WARM EARTH & SUNSET
    // ==========================================
    SUNSET_AMBER(
        displayName = "Golden Sunset Mirage",
        categoryTag = "Warm Horizon",
        description = "Warm glowing horizon amber with molten golden desert sunset gradients & arabesque star rays",
        isDark = true,
        background = Color(0xFF1C1004),
        surface = Color(0xFF2E1906),
        surfaceVariant = Color(0x33FFB300),
        surfacePressed = Color(0x54FFB300),
        outline = Color(0x59FFB300),
        textPrimary = Color(0xFFFFFBEB),
        textSecondary = Color(0xFFFDE68A),
        defaultAccent = Color(0xFFFFB300),
        previewColors = listOf(Color(0xFF1C1004), Color(0xFFD97706), Color(0xFFFFB300)),
        badgeLabel = "SUNSET GLOW",
        ornamentalPattern = OrnamentalPatternStyle.BYZANTINE_GOLD,
        ornamentalTintColor = Color(0xFFFFB300)
    ),
    MOCHA_ESPRESSO(
        displayName = "Warm Mocha Espresso",
        categoryTag = "Warm Earth",
        description = "Artisan roasted coffee beans with warm caramel latte crema glass & aroma curves",
        isDark = true,
        background = Color(0xFF170F0B),
        surface = Color(0xFF261A13),
        surfaceVariant = Color(0x33D97706),
        surfacePressed = Color(0x52D97706),
        outline = Color(0x4DD97706),
        textPrimary = Color(0xFFFEF3C7),
        textSecondary = Color(0xFFD4B896),
        defaultAccent = Color(0xFFD97706),
        previewColors = listOf(Color(0xFF170F0B), Color(0xFF78350F), Color(0xFFD4B896)),
        badgeLabel = "WARM MOCHA",
        ornamentalPattern = OrnamentalPatternStyle.SACRED_MANDALA,
        ornamentalTintColor = Color(0xFFD97706)
    ),

    // ==========================================
    // 11. OCEAN & TITANIUM
    // ==========================================
    DEEP_OCEAN(
        displayName = "Deep Oceanic Abyss",
        categoryTag = "Ocean",
        description = "Bioluminescent abyssal navy with vibrant electric aqua reef glass",
        isDark = true,
        background = Color(0xFF04101E),
        surface = Color(0xFF091F38),
        surfaceVariant = Color(0x3300E5FF),
        surfacePressed = Color(0x5200E5FF),
        outline = Color(0x4D00E5FF),
        textPrimary = Color(0xFFF0F9FF),
        textSecondary = Color(0xFFBAE6FD),
        defaultAccent = Color(0xFF00E5FF),
        previewColors = listOf(Color(0xFF04101E), Color(0xFF0369A1), Color(0xFF38BDF8)),
        badgeLabel = "OCEAN ABYSS",
        ornamentalPattern = OrnamentalPatternStyle.CONSTELLATION_NEBULA,
        ornamentalTintColor = Color(0xFF00E5FF)
    ),
    TITANIUM_SLATE(
        displayName = "Titanium Industrial Slate",
        categoryTag = "Industrial",
        description = "Brushed aircraft titanium monochrome with sleek metallic satin glass",
        isDark = true,
        background = Color(0xFF0F1216),
        surface = Color(0xFF1A1F26),
        surfaceVariant = Color(0x2B94A3B8),
        surfacePressed = Color(0x4794A3B8),
        outline = Color(0x4094A3B8),
        textPrimary = Color(0xFFF1F5F9),
        textSecondary = Color(0xFF94A3B8),
        defaultAccent = Color(0xFF94A3B8),
        previewColors = listOf(Color(0xFF0F1216), Color(0xFF475569), Color(0xFF94A3B8)),
        badgeLabel = "TITANIUM",
        ornamentalPattern = OrnamentalPatternStyle.MINIMALIST_AURA,
        ornamentalTintColor = Color(0xFF94A3B8)
    )
}

enum class ShapeStructureStyle(
    val displayName: String,
    val description: String
) {
    ROUNDED("Rounded Squircle", "Smooth 18dp modern rounded corners"),
    PILL("Pill / Capsule", "Continuous 50% capsule / stadium geometry"),
    LINE_BORDER("Line / Minimalist", "Crisp 6dp architectural line frame"),
    SHARP("Geometric Modern", "Sleek sharp 4dp beveled edges"),
    CHAMFER_CUT("Chamfer Cut Edge", "Futuristic 14dp diagonal cut corners"),
    TEARDROP("Teardrop Leaf", "Dynamic asymmetric organic corner geometry"),
    OVAL_STADIUM("Oval Stadium", "Ultra smooth 32dp deep curved geometry"),
    SOFT_BUBBLE("Soft Cloud Bubble", "Plump 26dp ultra-soft rounded corners")
}

fun ShapeStructureStyle.toButtonShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(18.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(50)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(8.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(4.dp)
    ShapeStructureStyle.CHAMFER_CUT -> CutCornerShape(14.dp)
    ShapeStructureStyle.TEARDROP -> RoundedCornerShape(topStart = 26.dp, topEnd = 6.dp, bottomEnd = 26.dp, bottomStart = 6.dp)
    ShapeStructureStyle.OVAL_STADIUM -> RoundedCornerShape(32.dp)
    ShapeStructureStyle.SOFT_BUBBLE -> RoundedCornerShape(26.dp)
}

fun ShapeStructureStyle.toInputShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(18.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(50)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(8.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(4.dp)
    ShapeStructureStyle.CHAMFER_CUT -> CutCornerShape(12.dp)
    ShapeStructureStyle.TEARDROP -> RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomEnd = 20.dp, bottomStart = 6.dp)
    ShapeStructureStyle.OVAL_STADIUM -> RoundedCornerShape(28.dp)
    ShapeStructureStyle.SOFT_BUBBLE -> RoundedCornerShape(22.dp)
}

fun ShapeStructureStyle.toCardShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(18.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(26.dp)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(8.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(4.dp)
    ShapeStructureStyle.CHAMFER_CUT -> CutCornerShape(16.dp)
    ShapeStructureStyle.TEARDROP -> RoundedCornerShape(topStart = 24.dp, topEnd = 8.dp, bottomEnd = 24.dp, bottomStart = 8.dp)
    ShapeStructureStyle.OVAL_STADIUM -> RoundedCornerShape(28.dp)
    ShapeStructureStyle.SOFT_BUBBLE -> RoundedCornerShape(24.dp)
}

fun ShapeStructureStyle.toSmallShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(8.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(50)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(4.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(2.dp)
    ShapeStructureStyle.CHAMFER_CUT -> CutCornerShape(6.dp)
    ShapeStructureStyle.TEARDROP -> RoundedCornerShape(topStart = 12.dp, topEnd = 3.dp, bottomEnd = 12.dp, bottomStart = 3.dp)
    ShapeStructureStyle.OVAL_STADIUM -> RoundedCornerShape(16.dp)
    ShapeStructureStyle.SOFT_BUBBLE -> RoundedCornerShape(12.dp)
}

fun ShapeStructureStyle.toChipShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(10.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(50)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(6.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(3.dp)
    ShapeStructureStyle.CHAMFER_CUT -> CutCornerShape(8.dp)
    ShapeStructureStyle.TEARDROP -> RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomEnd = 14.dp, bottomStart = 4.dp)
    ShapeStructureStyle.OVAL_STADIUM -> RoundedCornerShape(20.dp)
    ShapeStructureStyle.SOFT_BUBBLE -> RoundedCornerShape(16.dp)
}

enum class ComponentSizeStyle(
    val displayName: String,
    val description: String,
    val keypadSpacing: Dp,
    val keypadPaddingH: Dp,
    val keypadPaddingV: Dp,
    val keyFontScale: Float,
    val cardPadding: Dp,
    val inputHeight: Dp,
    val iconSize: Dp
) {
    COMPACT(
        "Compact (Dense)",
        "Dense layout with 8dp spacing, ideal for single-hand speed",
        keypadSpacing = 8.dp,
        keypadPaddingH = 12.dp,
        keypadPaddingV = 14.dp,
        keyFontScale = 0.88f,
        cardPadding = 10.dp,
        inputHeight = 48.dp,
        iconSize = 20.dp
    ),
    STANDARD(
        "Standard (Balanced)",
        "Default balanced Material 3 spacing and touch target dimensions",
        keypadSpacing = 12.dp,
        keypadPaddingH = 18.dp,
        keypadPaddingV = 20.dp,
        keyFontScale = 1.0f,
        cardPadding = 14.dp,
        inputHeight = 56.dp,
        iconSize = 24.dp
    ),
    LARGE(
        "Large (Comfort)",
        "Spacious 15dp spacing with enlarged buttons and easy reach",
        keypadSpacing = 15.dp,
        keypadPaddingH = 20.dp,
        keypadPaddingV = 22.dp,
        keyFontScale = 1.15f,
        cardPadding = 18.dp,
        inputHeight = 62.dp,
        iconSize = 26.dp
    ),
    EXTRA_LARGE(
        "Jumbo (Accessible)",
        "Maximized 18dp spacing and high-comfort large text touch targets",
        keypadSpacing = 18.dp,
        keypadPaddingH = 22.dp,
        keypadPaddingV = 26.dp,
        keyFontScale = 1.30f,
        cardPadding = 22.dp,
        inputHeight = 68.dp,
        iconSize = 30.dp
    )
}

enum class AnimationIntensity(
    val displayName: String,
    val description: String,
    val scaleFactor: Float,
    val translationFactor: Float
) {
    OFF("Disabled (Instant)", "No bounce or compression animations", 1.0f, 0f),
    SUBTLE("Subtle (0.6x)", "Gentle tactile micro-compression", 0.94f, 2.0f),
    BALANCED("Balanced (1.0x)", "Natural fluid spring rebound physics", 0.88f, 3.5f),
    HIGH_SPRING("High Spring (1.6x)", "Snappy bouncy compression and deep recoil", 0.80f, 5.0f),
    EXTREME_BOUNCE("Extreme Bounce (2.4x)", "Deep dynamic elastic bounce & kinetic energy", 0.72f, 7.5f)
}

enum class HapticIntensity(
    val displayName: String,
    val description: String
) {
    OFF("Disabled", "No haptic vibration"),
    LIGHT("Light", "Subtle haptic tick sensation"),
    MEDIUM("Medium", "Balanced standard tactile click"),
    STRONG("Strong", "Heavy tactile vibration pulse"),
    EXTREME("Extreme", "Maximum dual-pulse hardware vibration")
}

enum class SoundIntensity(
    val displayName: String,
    val description: String
) {
    OFF("Muted", "Silent mode without audio clicks"),
    SOFT("Soft (Discreet)", "Quiet acoustic tick sound"),
    STANDARD("Standard (Crisp)", "Crisp digital audio click"),
    LOUD("Loud (Resonant)", "High-resonance tactile chime"),
    EXTREME_BOOST("Extreme Boost (Maximum)", "100% volume dual acoustic burst - clears freely & loudly")
}

val LocalShapeStructure = staticCompositionLocalOf { ShapeStructureStyle.ROUNDED }
val LocalComponentSizing = staticCompositionLocalOf { ComponentSizeStyle.STANDARD }
val LocalAnimationIntensity = staticCompositionLocalOf { AnimationIntensity.BALANCED }
val LocalAppThemeSkin = staticCompositionLocalOf { AppThemeSkin.DARK_CHARCOAL }

@Composable
fun MyApplicationTheme(
    appThemeSkin: AppThemeSkin = AppThemeSkin.DARK_CHARCOAL,
    themeMode: ThemeMode = ThemeMode.DARK,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    componentSizeStyle: ComponentSizeStyle = ComponentSizeStyle.STANDARD,
    animationIntensity: AnimationIntensity = AnimationIntensity.OFF,
    fontStyle: AppFontStyle = AppFontStyle.BEGONIA,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val primary = appThemeSkin.defaultAccent
    val onPrimary = if (primary.luminance() > 0.5f) Color(0xFF0F172A) else Color.White

    val skinColorScheme = if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primary.copy(alpha = 0.2f),
            onPrimaryContainer = primary,
            secondary = primary.copy(alpha = 0.85f),
            onSecondary = onPrimary,
            background = appThemeSkin.background,
            onBackground = appThemeSkin.textPrimary,
            surface = appThemeSkin.surface,
            onSurface = appThemeSkin.textPrimary,
            surfaceVariant = appThemeSkin.surfaceVariant,
            onSurfaceVariant = appThemeSkin.textSecondary,
            outline = appThemeSkin.outline
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primary.copy(alpha = 0.15f),
            onPrimaryContainer = primary,
            secondary = primary.copy(alpha = 0.85f),
            onSecondary = onPrimary,
            background = if (appThemeSkin.isDark) PureLightBackground else appThemeSkin.background,
            onBackground = if (appThemeSkin.isDark) PureLightTextPrimary else appThemeSkin.textPrimary,
            surface = if (appThemeSkin.isDark) PureLightSurface else appThemeSkin.surface,
            onSurface = if (appThemeSkin.isDark) PureLightTextPrimary else appThemeSkin.textPrimary,
            surfaceVariant = if (appThemeSkin.isDark) PureLightGlassSurface else appThemeSkin.surfaceVariant,
            onSurfaceVariant = if (appThemeSkin.isDark) PureLightTextSecondary else appThemeSkin.textSecondary,
            outline = if (appThemeSkin.isDark) PureLightGlassBorder else appThemeSkin.outline
        )
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> skinColorScheme
    }

    val m3Shapes = Shapes(
        extraSmall = shapeStructureStyle.toSmallShape(),
        small = shapeStructureStyle.toInputShape(),
        medium = shapeStructureStyle.toCardShape(),
        large = shapeStructureStyle.toButtonShape(),
        extraLarge = shapeStructureStyle.toButtonShape()
    )

    val appTypography = createAppTypography(fontStyle)

    CompositionLocalProvider(
        LocalAppThemeSkin provides appThemeSkin,
        LocalShapeStructure provides shapeStructureStyle,
        LocalComponentSizing provides componentSizeStyle,
        LocalAnimationIntensity provides animationIntensity,
        LocalAppFontStyle provides fontStyle,
        LocalTextStyle provides appTypography.bodyMedium
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = appTypography,
            shapes = m3Shapes
        ) {
            ProvideTextStyle(value = appTypography.bodyMedium) {
                content()
            }
        }
    }
}

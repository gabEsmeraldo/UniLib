package com.example.unilib.models

data class ShelfCoordinate(
    val startDp: Int,
    val topDp: Int
)

data class LibrarySector(
    val code: String,
    val displayName: String,
    val shelves: List<String>
)

object LibraryLocationConfig {

    val sectors = listOf(
        LibrarySector(
            code = "A",
            displayName = "Tecnologia",
            shelves = listOf("A01", "A02", "A03", "A04")
        ),
        LibrarySector(
            code = "B",
            displayName = "Finanças e Administração",
            shelves = listOf("B01", "B02", "B03", "B04")
        ),
        LibrarySector(
            code = "C",
            displayName = "Literatura e Linguagens",
            shelves = listOf("C01", "C02", "C03", "C04")
        ),
        LibrarySector(
            code = "D",
            displayName = "Direito e Ciências Sociais",
            shelves = listOf("D01", "D02", "D03", "D04")
        ),
        LibrarySector(
            code = "E",
            displayName = "Saúde e Psicologia",
            shelves = listOf("E01", "E02", "E03", "E04")
        ),
        LibrarySector(
            code = "F",
            displayName = "Engenharia e Arquitetura",
            shelves = listOf("F01", "F02", "F03", "F04")
        ),
        LibrarySector(
            code = "G",
            displayName = "Educação e Humanas",
            shelves = listOf("G01", "G02", "G03", "G04")
        )
    )

    /*
     * Coordenadas aproximadas dentro do mapImageContainer.
     *
     * startDp = eixo horizontal
     * topDp = eixo vertical
     *
     * A lógica abaixo organiza as estantes em uma grade:
     * - A até G representam linhas/áreas do mapa.
     * - 01 até 04 representam estantes dentro daquela área.
     */
    val shelfCoordinates = mapOf(
        // Setor A - Tecnologia
        "A01" to ShelfCoordinate(startDp = 55, topDp = 285),
        "A02" to ShelfCoordinate(startDp = 95, topDp = 285),
        "A03" to ShelfCoordinate(startDp = 135, topDp = 285),
        "A04" to ShelfCoordinate(startDp = 175, topDp = 285),

        // Setor B - Finanças e Administração
        "B01" to ShelfCoordinate(startDp = 55, topDp = 315),
        "B02" to ShelfCoordinate(startDp = 95, topDp = 315),
        "B03" to ShelfCoordinate(startDp = 135, topDp = 315),
        "B04" to ShelfCoordinate(startDp = 175, topDp = 315),

        // Setor C - Literatura e Linguagens
        "C01" to ShelfCoordinate(startDp = 55, topDp = 345),
        "C02" to ShelfCoordinate(startDp = 95, topDp = 345),
        "C03" to ShelfCoordinate(startDp = 135, topDp = 345),
        "C04" to ShelfCoordinate(startDp = 175, topDp = 345),

        // Setor D - Direito e Ciências Sociais
        "D01" to ShelfCoordinate(startDp = 55, topDp = 375),
        "D02" to ShelfCoordinate(startDp = 95, topDp = 375),
        "D03" to ShelfCoordinate(startDp = 135, topDp = 375),
        "D04" to ShelfCoordinate(startDp = 175, topDp = 375),

        // Setor E - Saúde e Psicologia
        "E01" to ShelfCoordinate(startDp = 55, topDp = 405),
        "E02" to ShelfCoordinate(startDp = 95, topDp = 405),
        "E03" to ShelfCoordinate(startDp = 135, topDp = 405),
        "E04" to ShelfCoordinate(startDp = 175, topDp = 405),

        // Setor F - Engenharia e Arquitetura
        "F01" to ShelfCoordinate(startDp = 55, topDp = 435),
        "F02" to ShelfCoordinate(startDp = 95, topDp = 435),
        "F03" to ShelfCoordinate(startDp = 135, topDp = 435),
        "F04" to ShelfCoordinate(startDp = 175, topDp = 435),

        // Setor G - Educação e Humanas
        "G01" to ShelfCoordinate(startDp = 55, topDp = 465),
        "G02" to ShelfCoordinate(startDp = 95, topDp = 465),
        "G03" to ShelfCoordinate(startDp = 135, topDp = 465),
        "G04" to ShelfCoordinate(startDp = 175, topDp = 465)
    )

    fun getSectorByCode(code: String?): LibrarySector? {
        return sectors.firstOrNull { it.code == code }
    }

    fun getSectorByDisplayName(displayName: String?): LibrarySector? {
        return sectors.firstOrNull { it.displayName == displayName }
    }

    fun getSectorNames(): List<String> {
        return sectors.map { it.displayName }
    }

    fun getShelvesBySectorCode(code: String?): List<String> {
        return getSectorByCode(code)?.shelves.orEmpty()
    }

    fun getCoordinateByShelfCode(shelfCode: String?): ShelfCoordinate? {
        return shelfCoordinates[shelfCode]
    }

    fun buildLocationText(
        sectorCode: String?,
        shelfCode: String?,
        shelfLevel: String?
    ): String {
        val sectorName = getSectorByCode(sectorCode)?.displayName ?: "Setor não informado"
        val shelf = shelfCode?.takeIf { it.isNotBlank() } ?: "Estante não informada"
        val level = shelfLevel?.takeIf { it.isNotBlank() } ?: "Prateleira não informada"

        return "Setor $sectorName · Estante $shelf · Prateleira $level"
    }
}
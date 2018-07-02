/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define(function () {
	var doc_fr = "# language: fr\n" +
		"# Aide à la rédaction de cas de test Gherkin.\n" +
		"\n" +
		"# Gherkin est un langage implémentant la méthodologie du Behavior Driven Developement.\n" +
		"# Dans Squash TM, un cas de test Gherkin peut être exécuté manuellement comme un cas de test classique ou exporté pour une exécution automatisée externe.\n" +
		"\n" +
		"\n" +
		"# ---------------------- LEXIQUE DES MOTS CLEFS----------------------- \n" +
		"Fonctionnalité: \n" +
		"\n" +
		"# Mots clefs de définition de scénario : \n" +
		"# Scénario de test simple (sans jeux de données)\n" +
		"Scénario:\n" +
		"\n" +
		"# Scénario de test avec exemples (avec jeux de données)\n" +
		"Plan du Scénario:\n" +
		"\n" +
		"# Mot clef pour le jeu de données (à faire suivre d'une table de données)\n" +
		"Exemples:\n" +
		"\n" +
		"# Mot clef de contexte (Pas de test commun à tous les scénarii du script)\n" +
		"Contexte:\n" +
		"\n" +
		"# Mots clefs de pas de test de type précondition : \n" +
		"Soit\n" +
		"Etant donné que\n" +
		"Etant donné qu'\n" +
		"Etant données\n" +
		"Etant donnés\n" +
		"Etant donnée\n" +
		"Etant donné \n" +
		"\n" +
		"# Mots clefs de pas de test de type action : \n" +
		"Quand\n" +
		"Lorsque\n" +
		"Lorsqu'\n" +
		"\n" +
		"# Mot clef de pas de test de type résultat : \n" +
		"Alors\n" +
		"\n" +
		"# Mots clefs de pas de test de type continuation du pas de test en cours : \n" +
		"Et\n" +
		"Et que\n" +
		"Et qu'\n" +
		"Mais\n" +
		"Mais que\n" +
		"Mais qu'\n" +
		"*\n" +
		"\n" +
		"# ----------------------------- EXEMPLE DE FONCTIONNALITE ----------------------------- \n" +
		"# Exemple de structure d'une fonctionnalité complète avec trois scenarii de test et un contexte : \n" +
		"\n" +
		"# language: fr <- ligne de commentaire indiquant la localisation du script. Si absent ou invalide, Squash TM interprétera le script comme rédigé en Anglais.\n" +
		"\n" +
		"# Nom de la fonctionnalité et description optionnelle de la fonctionnalité (Attention à l'indentation si vous désirez une description) \n" +
		"Fonctionnalité: Vérifier la machine à café \n" +
		"\tIl s'agit de vérifier le bon fonctionnement de la machine à café.\n" +
		"\n" +
		"\t# Contexte optionnel. Si un contexte est présent, il sera réutilisé en tant que condition supplémentaire pour chaque scénario du script.\n" +
		"\tContexte:\n" +
		"\t\tSoit une machine à café.\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- EXEMPLE SCENARIO 1 -------------------------- \n" +
		"\t# Exemple de scénario simple\n" +
		"\tScénario: Vérifier que la machine est disponible.\n" +
		"\t\tEtant donné que la machine est branchée.\n" +
		"\t\tQuand je passe mon badge.\n" +
		"\t\tAlors je constate que mon solde s'affiche.\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- EXEMPLE SCENARIO 2 -------------------------- \n" +
		"\t# Exemple de scénario avec table de données sans paramétrage des pas de test\n" +
		"\tScénario: Vérifier les produits disponibles.\n" +
		"\t\tEtant donné que la machine est en marche.\n" +
		"\t\tQuand je liste les produits disponibles.\n" +
		"\t\tAlors je constate que tous les produits suivants sont disponibles :\n" +
		"\t\t| produit\t\t\t| prix  |\n" +
		"\t\t| Expresso\t\t| 0.40  |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50  |\n" +
		"\t\t| Cappuccino\t| 0.80  |\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- EXEMPLE SCENARIO 3 -------------------------- \n" +
		"\t# Exemple de scénario avec table de données et paramétrage des pas de test\n" +
		"\t# A l'exécution, les valeurs de paramètres entre <> seront substituées\n" +
		"\t# Le scénario sera joué une fois pour chaque jeu de données\n" +
		"\tPlan du Scénario: Vérifier la livraison des produits.\n" +
		"\t\tEtant donné que la machine est en marche.\n" +
		"\t\tEt que mon solde est au moins de <prix>.\n" +
		"\t\tQuand je sélectionne le <produit>.\n" +
		"\t\tAlors la machine me sert un <produit> et mon compte est débité de <prix>.\n" +
		"\t\tExemples:\n" +
		"\t\t| produit\t\t\t| prix  |\n" +
		"\t\t| Expresso\t\t| 0.40  |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50  |\n" +
		"\t\t| Cappuccino\t| 0.80  |\n" +
		"\n";

	var doc_en = "# language: en\n" +
		"# Quick reference guide to writing Gherkin test cases\n" +
		"\n" +
		"# Gherkin is a language based on the process of Behaviour Driven Development.\n" +
		"# When using Squash TM, a Gherkin test case can either be executed manually like any regular test case or exported for use in an external automated execution.\n" +
		"\n" +
		"\n" +
		"# ---------------------- KEYWORDS LEXICON ----------------------- \n" +
		"Feature: \n" +
		"\n" +
		"# Scenario-defining Keywords : \n" +
		"# Simple test scenario (no dataset)\n" +
		"Scenario:\n" +
		"\n" +
		"# Test scenario with examples (and datasets)\n" +
		"Scenario Outline:\n" +
		"\n" +
		"# Keyword for a dataset (to be followed by a data table)\n" +
		"Examples:\n" +
		"\n" +
		"# Context keyword (Test steps common to all script scenarios)\n" +
		"Background:\n" +
		"\n" +
		"# Keyword for test steps preconditions :\n" +
		"Given\n" +
		"\n" +
		"# Keyword for test steps actions :\n" +
		"When\n" +
		"\n" +
		"# Keyword for test steps results : \n" +
		"Then\n" +
		"\n" +
		"# Keyword for test steps to continue the ongoing test step :\n" +
		"And\n" +
		"But\n" +
		"*\n" +
		"\n" +
		"# ----------------------------- FEATURE EXAMPLE ----------------------------- \n" +
		"# Structure example of a complete feature with three test scenarios and a context :\n" +
		"\n" +
		"# language: en <- this comment line indicates the localization of the script. By default (if missing or invalid), Squash TM will consider that the script is written in English.\n" +
		"\n" +
		"# Feature name and its (optional) description (Beware the indentation if you wish to include a description)\n" +
		"Feature: Check the coffee machine\n" +
		"\tThe aim is to check that the coffee machine works properly.\n" +
		"\n" +
		"\t# Optional context. If a context is present here, it will be re-used as an additional condition for each script scenario.\n" +
		"\tBackground:\n" +
		"\t\tGiven a coffee machine\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- SCENARIO EXAMPLE N°1 -------------------------- \n" +
		"\t# Simple scenario example\n" +
		"\tScenario: Check that the machine is available.\n" +
		"\t\tGiven the machine is operating.\n" +
		"\t\tWhen I use my badge.\n" +
		"\t\tThen I can check my balance.\n" +
		"\t\t\n" +
		"\n" +
		"\t# -------------------------- SCENARIO EXAMPLE N°2 -------------------------- \n" +
		"\t# Example of a scenario with a data table but no configuration of test steps\n" +
		"\tScenario: Check what products are available.\n" +
		"\t\tGiven the machine is operating.\n" +
		"\t\tWhen I list the available products.\n" +
		"\t\tThen I notice that the following products are available :\n" +
		"\t\t| product\t\t| price |\n" +
		"\t\t| Expresso\t\t| 0.40  |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50  |\n" +
		"\t\t| Cappuccino\t| 0.80  |\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- SCENARIO EXAMPLE N°3 -------------------------- \n" +
		"\t# Example of a scenario with a data table and configuration of test steps\n" +
		"\t# Upon execution, the parameters' values in between <> will be substituted\n" +
		"\t# The scenario will be played once for each dataset (that is to say each line)\n" +
		"\tScenario Outline: Check the delivery of products.\n" +
		"\t\tGiven the machine is operating\n" +
		"\t\tAnd my account contains at least <price>.\n" +
		"\t\tWhen I select <product>.\n" +
		"\t\tThen the machine delivers me a <product> and my account is charged <price>.\n" +
		"\t\tExamples:\n" +
		"\t\t| product\t\t\t| price |\n" +
		"\t\t| Expresso\t\t| 0.40  |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50  |\n" +
		"\t\t| Cappuccino\t| 0.80  |\n";

	var doc_es = "# language: es\n" +
		"# Asistencia para el proceso de escritura de los casos de prueba Gherkin.\n" +
		"\n" +
		"# Gherkin es un lenguaje implementando las metodologías del Behaviour Driven Development.\n" +
		"# En Squash TM, un caso de prueba Gherkin puede ejecutarse manualmente de la misma manera que un caso de prueba clásico o ser exportado para una ejecución externa automatizada.\n" +
		"\n" +
		"# ---------------------- LÉXICO DE TÉRMINOS -----------------------\n" +
		"Característica:\n" +
		"\n" +
		"# Términos para definir a los escenarios:\n" +
		"# Escenarios de prueba simples (sin dataset)\n" +
		"Escenario:\n" +
		"# Escenarios de test con ejemplos (y con dataset)\n" +
		"Esquema del escenario:\n" +
		"# Término para definir un dataset (debe acompañarse de una tabla de datos)\n" +
		"Ejemplos\n" +
		"\n" +
		"# Término para definir un contexto (pasos de prueba comunes pare todos los escenarios del script)\n" +
		"Antecedentes:\n" +
		"\n" +
		"# Términos de precondiciones para pasos de prueba:\n" +
		"Dado\n" +
		"Dada\n" +
		"Dados\n" +
		"Dadas\n" +
		"\n" +
		"# Término de acción para pasos de prueba:\n" +
		"Cuando\n" +
		"\n" +
		"# Término de resultado para pasos de prueba:\n" +
		"Entonces\n" +
		"\n" +
		"# Términos de continuación de los pasos de prueba en curso\n" +
		"Y\n" +
		"E\n" +
		"\n" +
		"# ------------------------- EJEMPLO DE CARACTERISTICA ------------------------- \n" +
		"# Ejemplo de estructura de una característica completa con tres escenarios de prueba y un contexto:\n" +
		"\n" +
		"# language: es <- Esta línea de comentario indica la localización del script. Por defecto (cuando falta o es incorrecta), Squash TM interpretará el script como si era en inglés.\n" +
		"\n" +
		"# Nombre de la característica y su descripción facultativa (Cuidado con el sangrado si quieren implementar a una descripción)\n" +
		"Característica: Comprobar la máquina de café\n" +
		"\tAsegurarse de qué la máquina de café funciona correctamente\n" +
		"\n" +
		"\t# Contexto facultativo. Cuando un contexto existe, será reutilizado como condición suplementaria para cada escenario del script.\n" +
		"\tAntecedentes:\n" +
		"\t\tDado una máquina de café\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- ESCENARIO DE EJEMPLO 1 -------------------------- \n" +
		"\t# Ejemplo de escenario simple\n" +
		"\tEscenario: Verificar que la máquina de café es disponible.\n" +
		"\t\tDado que la máquina está conectada.\n" +
		"\t\tCuando utilizo mi tarjeta.\n" +
		"\t\tEntonces constato que mi saldo se indica.\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- ESCENARIO DE EJEMPLO 2 -------------------------- \n" +
		"\t# Ejemplo de escenario con tabla de datos sin configuración de los pasos de prueba\n" +
		"\tEscenario: Controlar cuales son los productos disponibles.\n" +
		"\t\tDado que la máquina está conectada.\n" +
		"\t\tCuando listo los productos disponibles.\n" +
		"\t\tEntonces constato que todos los productos siguientes son disponibles:\n" +
		"\t\t| producto\t\t| precio |\n" +
		"\t\t| Espresso\t\t|  0.40  |\n" +
		"\t\t| Lungo\t\t\t\t|  0.50  |\n" +
		"\t\t| Cappuccino\t|  0.80  |\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- ESCENARIO DE EJEMPLO 3 -------------------------- \n" +
		"\t# Ejemplo de escenario con tabla de datos y pasos de prueba configurados\n" +
		"\t# Los valores de los parámetros entre <> serán sustituidos a la ejecución\n" +
		"\t# El escenario estará ejecutado una vez para cada dataset\n" +
		"\tEsquema del escenario: Verificar la entrega de los productos\n" +
		"\t\tDado que la máquina está conectada.\n" +
		"\t\tY que mi saldo es de <precio> por lo menos.\n" +
		"\t\tCuando selecciono el <producto>.\n" +
		"\t\tEntonces la máquina me sirve un <producto> y le cargó el <precio> a mi cuenta.\n" +
		"\t\tEjemplos:\n" +
		"\t\t| producto\t\t| precio |\n" +
		"\t\t| Espresso\t\t|  0.40  |\n" +
		"\t\t| Lungo\t\t\t\t|  0.50  |\n" +
		"\t\t| Cappuccino\t|  0.80  |\n";

	var doc_de = "# language: de\n" +
		"# Anleitung zur Erstellung von Testfällen mit Gherkin.\n" +
		"\n" +
		"# Gherkin ist eine Programmiersprache, die die Behavior Driven Development Methodologie implementiert.\n" +
		"# Mit Squash TM kann ein Gherkin-Testfall entweder manuell durchgeführt werden, genauso wie ein klassischer Testfall, oder exportiert werden, für eine automatisierte Durchführung außerhalb von Squash TM.\n" +
		"\n" +
		"\n" +
		"# ---------------------- SCHLÜSSELWÖRTER VERZEICNHIS ----------------------- \n" +
		"Funktionalität: \n" +
		"\n" +
		"# Sprachelemente um Szenarien zu definieren: \n" +
		"# Einfaches Szenario (ohne Datensatz)\n" +
		"Szenario:\n" +
		"\n" +
		"# Testszenario (mit Datensatz)\n" +
		"Szenariogrundriss:\n" +
		"\n" +
		"# Schlüsselwort für einen Datensatz (Eine Datentabelle muss folgen)\n" +
		"Beispiele:\n" +
		"\n" +
		"# Schlüsselwort für eine Grundlage (Mit jedem Szenario des Skriptes gemeinsamer Testschritt)\n" +
		"Grundlage:\n" +
		"\n" +
		"# Schlüsselwörter für Testschritte des Types „Vorbedingung“: \n" +
		"Angenommen\n" +
		"Gegeben sei\n" +
		"Gegeben seien\n" +
		"\n" +
		"# Schlüsselwort für Testschritte des Types „Ereignis oder Handlung“:\n" +
		"Wenn\n" +
		"\n" +
		"# Schlüsselwort für Testschritte des Types „Ergebnis“:\n" +
		"Dann\n" +
		"\n" +
		"# Schlüsselwort für Testschritte des Types „Fortsetzung“: \n" +
		"Und\n" +
		"Aber\n" +
		"*\n" +
		"\n" +
		"# ----------------------------- BEISPIELFUNKTIONNALITÄT ----------------------------- \n" +
		"# Beispielstruktur einer Funktionalität mit drei Szenarien und eine Grundlage:\n" +
		"\n" +
		"# language: de <- Angabe der Sprache. Wenn abwesend oder ungültig entscheidet sich Squash TM für Englisch.\n" +
		"\n" +
		"# Name der Funktionalität und optionale Beschreibung (Passen Sie auf die Einrückung auf, wenn Sie eine Beschreibung eingeben).\n" +
		"Funktionalität: Die Kaffeemaschine prüfen \n" +
		"\tEs handelt sich darum, das gute Funktionieren der Kaffeemaschine zu überprüfen.\n" +
		"\n" +
		"\t# optionale Grundlage. Wenn eine Grundlage anwesend ist, wird sie als zusätzliche Bedingung für jedes Szenario des Skriptes hinzugefügt.\n" +
		"\tGrundlage:\n" +
		"\t\tGegeben sei eine Kaffeemaschine.\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- BEISPIELSZENARIO 1 -------------------------- \n" +
		"\t# Beispiel für ein einfaches Szenario\n" +
		"\tSzenario: Überprüfung, dass die Kaffeemaschine zur Verfügung steht.\n" +
		"\t\tGegeben sei, dass die Maschine angeschlossen ist.\n" +
		"\t\tWenn ich meine Karte vorstelle.\n" +
		"\t\tDann stelle ich fest, dass mein Kontostand angezeigt wird.\n" +
		"\n" +
		"\t# -------------------------- BEISPIELSZENARIO 2 -------------------------- \n" +
		"\t# Beispielszenario mit Datentabelle ohne Platzhalter\n" +
		"\tSzenario: Überprüfung der Verfügbarkeit von Produkten.\n" +
		"\t\tGegeben sei, dass die Maschine zur Verfügung steht.\n" +
		"\t\tWenn ich die verfügbaren Produkte aufliste.\n" +
		"\t\tDann stelle ich fest, dass alle folgenden Produkte verfügbar sind:\n" +
		"\t\t| Produkt\t\t\t| Preis  |\n" +
		"\t\t| Espresso\t\t| 0.40   |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50   |\n" +
		"\t\t| Cappuccino\t| 0.80   |\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- BEISPIELSZENARIO 3 -------------------------- \n" +
		"\t# Beispielszenario mit Datentafel mit Platzhalter in Testschritten\n" +
		"\t# Während des Durchlaufs werden die Werte der Parameter zwischen <> substituiert\n" +
		"\t# Das Szenario wird einmal für jeden Datensatz gespielt sein\n" +
		"\tSzenariogrundriss: die Lieferung der Produkte überprüfen.\n" +
		"\t\tGegeben sei die Maschine steht zur Verfügung.\n" +
		"\t\tUnd mein Kontostand mindestens <Preis> beträgt.\n" +
		"\t\tWenn ich <Produkt> auswähle.\n" +
		"\t\tDann spendet mir die Maschine ein <Produkt> und <Preis> wird von meinem Konto abgebucht.\n" +
		"\t\tBeispiele:\n" +
		"\t\t| Produkt\t\t\t| Preis  |\n" +
		"\t\t| Espresso\t\t| 0.40   |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50   |\n" +
		"\t\t| Cappuccino\t| 0.80   |\n" +
		"\n";

	function getDocumentation(locale) {
		switch (locale) {
			case "fr":
				return doc_fr;
			case "es":
				return doc_es;
			case "de":
				return doc_de;
			default :
				return doc_en;
		}
	}

	return {
		getDocumentation: getDocumentation
	};
});

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
	var doc_fr =
		"# language: fr\n" +
		"# Aide à la rédaction de cas de test Gherkin.\n\n" +
		"# Gherkin est un langage implémentant la méthodologie du Behavior Driven Developement.\n" +
		"# Dans Squash TM un cas de test Gherkin peut être exécuté manuellement comme un cas de test classique ou exporté pour une excecution automatisée externe.\n\n\n" +
		"# ---------------------- LEXIQUE DES MOTS CLEFS----------------------- \n" +
		"Fonctionnalité: \n\n"+
		"# Mots clefs de definition de scenario : \n"+
		"# Scénario de test simple (sans jeu de données)\n" +
		"Scénario:\n"+
		"# Scénario de test avec exemples (avec jeux de données)\n" +
		"Plan du Scénario:\n"+
		"# Mot clef pour le jeu de données (à faire suivre d'une table de données)\n" +
		"Exemples:\n\n"+
		"# Mot clef de contexte (Pas de test commun à tous les scénarios du script)\n" +
		"Contexte:\n\n"+
		"# Mots clefs de pas de test de type précondition : \n"+
		"Soit\n"+
		"Etant donné que\n"+
		"Etant donné qu'\n"+
		"Etant données\n"+
		"Etant donnés\n"+
		"Etant donnée\n"+
		"Etant donné \n\n"+
		"# Mots clefs de pas de test de type action : \n"+
		"Quand\n"+
		"Lorsque\n"+
		"Lorsqu'\n\n"+
		"# Mots clefs de pas de test de type résultat : \n"+
		"Alors\n\n"+
		"# Mots clefs de pas de test de type continuation du pas de test en cours : \n"+
		"Et\n"+
		"Et que\n"+
		"Et qu'\n"+
		"Mais\n"+
		"Mais que\n"+
		"Mais qu'\n"+
		"*\n"+

		"\n# ----------------------------- EXEMPLE DE FEATURE ----------------------------- \n" +
		"# Exemple de structure d'une feature complète avec trois scenari de tests et un contexte: \n\n" +
		"# language: fr <- ligne de commentaire indiquant la localisation du script. Si absent ou invalide Squash TM interpretera le script comme rédigé en Anglais.\n\n" +
		"# Nom de la fonctionnalité et description optionelle de la fonctionnalité (Attention à l'indentation si vous desirez une description) \n" +
		"Fonctionnalité: Vérifier la machine à café \n" +
		"\tIl s'agit de vérifier le bon fonctionnement de la machine à café.\n\n" +
		"\t# Contexte optionnel. Si un contexte est présent, il sera réutilisé en tant que condition supplémentaire pour chaque scenario du script.\n" +
		"\tContexte:\n" +
		"\t\tSoit une machine à café.\n\n" +
		"\n\t# -------------------------- EXEMPLE SCENARIO 1 -------------------------- \n" +
		"\t# Exemple de scenario simple\n" +
		"\tScénario: Vérifier que la machine est disponible.\n" +
		"\t\tEtant donné que la machine est branchée.\n" +
		"\t\tQuand je passe mon badge.\n" +
		"\t\tAlors je constate que mon solde s'affiche.\n\n" +
		"\n\t# -------------------------- EXEMPLE SCENARIO 2 -------------------------- \n" +
		"\t# Exemple de scenario avec table de donnée sans paramétrage des pas de test\n" +
		"\tScénario: Vérifier les produits disponibles.\n" +
		"\t\tEtant donné que la machine est en marche.\n" +
		"\t\tQuand je liste les produits disponibles.\n" +
		"\t\tAlors je constate que tous les produits suivants sont disponibles :\n" +
		"\t\t| produit\t\t\t| prix  |\n" +
		"\t\t| Expresso\t\t| 0.40  |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50  |\n" +
		"\t\t| Cappuccino\t| 0.80  |\n\n" +
		"\n\t# -------------------------- EXEMPLE SCENARIO 3 -------------------------- \n" +
		"\t# Exemple de scénario avec table de données et paramétrage des pas de test\n" +
		"\t# A l'éxécution les valeurs de paramètres entre <> seront substitées\n" +
		"\t# Le scenario sera joué une fois pour chaque jeu de données\n" +
		"\tPlan du Scénario: Vérifier la livraison des produits.\n" +
		"\t\tEtant donné que la machine est en marche.\n" +
		"\t\tEt que mon solde est au moins de <prix>.\n" +
		"\t\tQuand je selectionne le <produit>.\n" +
		"\t\tAlors la machine me sert un <produit> et mon compte est débité de <prix>.\n" +
		"\t\tExemples:\n" +
		"\t\t| produit\t\t\t| prix  |\n" +
		"\t\t| Expresso\t\t| 0.40  |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50  |\n" +
		"\t\t| Cappuccino\t| 0.80  |\n";

	var doc_en = "# language: en\n" +
		"# Assistance for the writing of Gherkin test cases\n" +
		"\n" +
		"# Gherkin is a language utilizing the methodology of Behaviour Driven Development.\n" +
		"# When using Squash TM, A Gherkin test case can either be executed manually like any regular test case or exported for use in an external automated execution.\n" +
		"\n" +
		"\n" +
		"# ---------------------- KEYWORDS LEXICON ----------------------- \n" +
		"Feature: \n" +
		"\n" +
		"# Scenario-defining Keywords : \n" +
		"# Simple test scenario (no dataset)\n" +
		"Scenario:\n" +
		"# Test scenario with exemples (and datasets)\n" +
		"Scenario Outline:\n" +
		"# Keyword for a dataset (to be followed with a data table)\n" +
		"Examples:\n" +
		"\n" +
		"# Context keyword (Test steps that are common to all the scenarios within the script)\n" +
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
		"# Keyword for requirement to continue the ongoing test step :\n" +
		"And\n" +
		"But\n" +
		"*\n" +
		"\n" +
		"# ----------------------------- FEATURE EXAMPLE ----------------------------- \n" +
		"# Structure example of a feature complete with three test scenarios and a context :\n" +
		"\n" +
		"# language: en <- this comment line indicate the localization of the script. By default (if missing or invalid), Squash TM will consider the script to be written in English.\n" +
		"\n" +
		"# Feature name and its (optional) description (Beware indentation if you wish to include a description)\n" +
		"Feature: Check the coffee machine\n" +
		"\tThe aim is to check that the coffee machine functions properly.\n" +
		"\n" +
		"\t# Optional context. If a context is present here, it will be re-used as an additional condition for each scenario of the script.\n" +
		"\tBackground:\n" +
		"\t\tGiven a coffee machine\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- SCENARIO EXAMPLE N°1 -------------------------- \n" +
		"\t# Simple scenario example\n" +
		"\tScenario: Check that the machine is available.\n" +
		"\t\tGiven that the machine is operating.\n" +
		"\t\tWhen I use identify my badge.\n" +
		"\t\tThen I can check my balance.\n" +
		"\t\t\n" +
		"\n" +
		"\t# -------------------------- SCENARIO EXAMPLE N°2 -------------------------- \n" +
		"\t# Example of a scenario with a data table but no configuration of test steps\n" +
		"\tScenario: Check what products are available.\n" +
		"\t\tGiven that the machine is operating.\n" +
		"\t\tWhen I list the available products.\n" +
		"\t\tThen I notice that the following products are available :\n" +
		"\t\t| products\t\t| price |\n" +
		"\t\t| Expresso\t\t| 0.40  |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50  |\n" +
		"\t\t| Cappuccino\t| 0.80  |\n" +
		"\n" +
		"\n" +
		"\t# -------------------------- SCENARIO EXAMPLE N°3 -------------------------- \n" +
		"\t# Example of a scenario with a data table and configuration of test steps\n" +
		"\t# Upon execution, the parameters' values in between <> will be substituted\n" +
		"\t# The scenario will be played once for each and every dataset\n" +
		"\tScenario Outline: Check the delivery of products.\n" +
		"\t\tGiven that the machine is operating\n" +
		"\t\tAnd that my balance is of at least <price>.\n" +
		"\t\tWhen I select <product>.\n" +
		"\t\tThen the machine delivers me a <product> and my balance is reduced by <price>.\n" +
		"\t\tExamples:\n" +
		"\t\t| products\t\t| price |\n" +
		"\t\t| Expresso\t\t| 0.40  |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50  |\n" +
		"\t\t| Cappuccino\t| 0.80  |\n";

	function getDocumentation(locale) {
		switch (locale) {
			case "fr":
				return doc_fr;
			default :
				return doc_en;
		}
	}

	return {
		getDocumentation: getDocumentation
	};
});

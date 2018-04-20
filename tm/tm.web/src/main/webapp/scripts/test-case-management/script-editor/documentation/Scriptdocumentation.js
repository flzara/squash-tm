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
		"# Exemple de structure : \n\n" +
		"# language: fr <- ligne de commentaire indiquant la localisation du script. Si absent ou invalide Squash TM interpretera le script comme rédigé en Anglais.\n\n" +
		"# Nom de la fonctionnalité et description optionelle de la fonctionnalité (Attention à l'indentation si vous desirez une description) \n" +
		"Fonctionnalité: Vérifier la machine à café \n" +
		"\tIl s'agit de vérifier le bon fonctionnement de la machine à café.\n\n" +
		"\t# Contexte optionnel. Si un contexte est présent, il sera réutilisé en tant que condition supplémentaire pour chaque scenario du script.\n" +
		"\tContexte:\n" +
		"\t\tSoit une machine à café.\n\n" +
		"\t# Exemple de scenario simple\n" +
		"\tScénario: Vérifier que la machine est disponible.\n" +
		"\t\tEtant donné que la machine est branchée.\n" +
		"\t\tQuand je passe mon badge.\n" +
		"\t\tAlors je constate que mon solde s'affiche.\n\n" +
		"\t# Exemple de scenario avec table de donnée sans paramétrage des pas de test\n" +
		"\tScénario: Vérifier les produits disponibles.\n" +
		"\t\tEtant donné que la machine est en marche.\n" +
		"\t\tQuand je liste les produits disponibles.\n" +
		"\t\tAlors je constate que tous les produits suivants sont disponibles :\n" +
		"\t\t| produit\t\t\t| prix  |\n" +
		"\t\t| Expresso\t\t| 0.40  |\n" +
		"\t\t| Lungo\t\t\t\t| 0.50  |\n" +
		"\t\t| Cappuccino\t| 0.80  |\n\n" +
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
	var doc_en = "documentation en";

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

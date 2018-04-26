Feature: verifier la machine encore
  Il s'agit de tester le bon fonctionnement de la machine à café

  Background:
    Given La machine est branchée

  Scenario: Verification des produits

    When j'affiche la liste des produits

    Then j'obtiens bien la liste ci dessous
        |produit|prix|
        | chocolat|0.35|
        | thé|0.50|


  Scenario: Verification des produits

    Given J'ai un badge spécial

    When j'affiche la liste des produits

    Then j'obtiens bien la liste ci dessous
        |produit|prix|
        | chocolat|0.35|
        | thé|0.50|
        | café surcafeïné|2|
        | Mega red bull|6|

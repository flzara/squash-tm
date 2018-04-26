Feature: Aller chercher un café
  Il s'agit de tester le bon fonctionnement de la machine à café

  Scenario Outline:Cas standard, paiement par pièces
    C'est le cas classique de paiement direct à la machine, sans passer par un badge entreprise

    Given J'ai un badge valide
    And La machine n'est pas en panne
    And J'ai de l'argent dans ma poche

    When Je mets <montant> dans le monnayeur
    And Je sélectionne le produit <produit>

    Then J'obtiens bien le <produit>

    Examples:
      |montant|produit|
      | 0.35|café long|
      | 0.50|cappuccino|
      | 1.00|café macchiato spécial|
      | 0.10|café beurk|



  Scenario:Cas non passant, la machine est en panne

    Given J'ai un badge valide
    And La machine est en panne
    And J'ai de l'argent dans ma poche

    When Je sélectionne un produit

    Then La machine émet un bip plaintif et je serais fatigué par manque de caféïne

  Scenario:Cas special
    Given c'est un cas vraiment "special"
    """ this
    is
    a docstring argumant
    """
    When je veux mon café
    Then c'est pas l'heure

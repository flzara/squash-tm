Feature: Aller chercher un café
  Il s'agit de tester le bon fonctionnement de la machine à café

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

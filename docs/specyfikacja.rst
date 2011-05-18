=============================================
Sztuczna inteligencja - Specyfikacja projektu
=============================================
Temat: "Hierarchiczne grupowanie danych"
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

:Autorzy: Rafał Selewońko, Paweł Tomkiel
:Grupa: PS8 
:Prowadzący: mgr inż. Dariusz Małyszko

.. spis treści::

Wprowadzanie i pobieranie danych
--------------------------------

Projekt uruchamia się widokiem pod-aplikacji pozwalającej na wybór 
katalogu, w którym przechowywane są pliki danych. Wszystkie pliki są 
wylistowane w lewej części okna i można wybrać jeden z nich do 
wyświetlenia w jego prawej części. Istnieje osobny przycisk do 
załadowania katalogu i osobny do wyświetlenie pliku.

Pobieranie parametrów zadania
-----------------------------

Po załadowaniu pliku będzie możliwe(projekt jest nadal w fazie 
budowania) uruchomienie aplikacji głównej zajmującej się obliczeniami. 
Aktualnie wczytany plik w pod-aplikacji jest przekazywany do aplikacji 
głównej. Następnie będzie przetwarzany zgodnie z algorytmem grupowania 
hierarchicznego danych i będą wyświetlane oraz wizualizowane wyniki 
obliczeń.

Obliczenia
----------

Algorytm grupujący hierarchicznie dane opiera się na kilku prostych 
krokach::

    1. Tworzy m grup zawierających po jednym obiekcie
    2. Wyznacza odległości między grupami
    3. Znajduje parę najbliższych grup i łączy je w jedną grupę
    4. Wyznacz jeszcze raz odległości między nową grupą a pzoostałymi grupami
    5. Powtarzaj kroki aż do momentu gdy pozostanie zadana liczba grup

Finalizacja
-----------

Po przeprowadzeniu wszystkich obliczeń i pogrupowaniu obiektów projekt 
będzie wizualizował(w osobnym oknie) w przestrzeni dwuwymiarowej 
odległości między 
obiektami i będzie je łączył w grupy. Forma tekstowa wyników obliczeń 
też jest w planach, najpewniej w formie tabeli.

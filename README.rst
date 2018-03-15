================================================================================
  Dative in re-frame
================================================================================

A `re-frame`_ implementation of `Dative`_.

Setup and run
================================================================================

1. Install `Leiningen`_ (plus Java).

2. Get the Dative source code::

       $ git clone https://github.com/dativebase/dative-re-frame.git

3. Clean build::

       $ lein do clean, figwheel

4. Run. You'll have to wait for step (3) to do its compile, but then::

       $ open http://localhost:3451


Compile an optimised version
================================================================================

1. Compile::

       $ lein do clean, with-profile prod compile

2. Open the following in your browser: ``resources/public/index.html``


Exploring the code
================================================================================

From the re-frame README:

    To build a re-frame app, you:

    - design your app's data structure (data layer)
    - write and register subscription functions (query layer)
    - write Reagent component functions (view layer)
    - write and register event handler functions (control layer and/or state
      transition layer)

In ``src``, there's a matching set of files (each small)::

    src
    ├── core.cljs         <--- entry point, plus history
    ├── db.cljs           <--- data related  (data layer)
    ├── subs.cljs         <--- subscription handlers  (query layer)
    ├── views.cljs        <--- reagent  components (view layer)
    └── events.cljs       <--- event handlers (control/update layer)


Notes
================================================================================

- The [official reagent example](https://github.com/reagent-project/reagent/tree/master/examples/todomvc)
- The `official reagent example`_
- Look at the `re-frame Wiki`_


.. _`re-frame Wiki`: https://github.com/Day8/re-frame/wiki
.. _`official reagent example`: https://github.com/reagent-project/reagent/tree/master/examples/todomvc)
.. _`re-frame`: https://github.com/Day8/re-frame
.. _`Dative`: https://github.com/dativebase/dative
.. _`Leiningen`: http://leiningen.org/

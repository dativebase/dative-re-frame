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

       $ lein clean
       $ lein figwheel dev

4. Run. You'll have to wait for step (3) to do its compile, but then::

       $ open http://localhost:3450/

5. To make the garden ClojureScript auto-compile to CSS::

       $ lein garden auto


Production Build
================================================================================

To compile ClojureScript to JavaScript::

    lein clean
    lein cljsbuild once min


Exploring the code
================================================================================

From the re-frame README:

    To build a re-frame app, you:

    - design your app's data structure (data layer)
    - write and register subscription functions (query layer)
    - write Reagent component functions (view layer)
    - write and register event handler functions (control layer and/or state
      transition layer)

In ``src/cljs/dative``, there's a matching set of files (each small)::

    src/cljs/dative
    ├── core.cljs           <--- entry point, plus history
    ├── db.cljs             <--- data related (data layer)
    ├── subs.cljs           <--- subscription handlers (query layer)
    ├── login.cljs          <--- reagent components (view layer)
    ├── old-instances.cljs  <--- reagent components (view layer)
    └── events.cljs         <--- event handlers (control/update layer)


Useful documents
================================================================================

- the `re-com demo page`_
- the `re-frame README`_
- the `official reagent example`_


.. _`re-com demo page`: http://re-demo.s3-website-ap-southeast-2.amazonaws.com/
.. _`re-frame README`: https://github.com/Day8/re-frame/blob/master/README.md
.. _`official reagent example`: https://github.com/reagent-project/reagent/tree/master/examples/todomvc
.. _`re-frame`: https://github.com/Day8/re-frame
.. _`Dative`: https://github.com/dativebase/dative
.. _`Leiningen`: http://leiningen.org/

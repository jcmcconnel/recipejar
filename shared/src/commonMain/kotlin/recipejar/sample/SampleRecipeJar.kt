package recipejar.sample
import recipejar.domain.Recipe
import recipejar.html.RecipeSerializer

/**
 * Bundled sample "family jar" for mobile prototypes.
 * HTML is real RecipeJar corpus content; titles/body come from [RecipeSerializer.parse].
 */
data class SampleRecipeEntry(
    val filename: String,
    val html: String,
)

object SampleRecipeJar {
    val entries: List<SampleRecipeEntry> = listOf(
        SampleRecipeEntry(
            filename = "BananaBread.html",
            html = """
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="generator" content="RecipeJar"/>
    <meta name="labels" content="Bread"/>
    <meta name="created" content="Sometime before, Sun Jan 17 08:57:23 EST 2010"/>
    <meta name="last saved" content="Sun Jan 17 12:37:09 EST 2010"/>
    <meta name="generator" content="RecipeJar"/>
    <title>Banana Bread</title>
    <style type="text/css">@import url("style/default.css");</style>
    
  </head>
   <body>
    <div id="header"><h1>Banana Bread</h1></div>
    <div id="notes-header"><h3>Notes:</h3></div>
    <div id="notes"><p>     How to Cook Everything.</p>                              </div>
    <div id="notes-footer"></div>
    <div id="ingredients-header"><h3>You will need:</h3></div>
    <div id="ingredients">
      <ul>
         <li><span class="qty">8</span> <span class="unit">Tbsps</span> <span class="name">(1 stick) Butter</span></li>
         <li><span class="qty">1 1/2</span> <span class="unit">Cups</span> <span class="name">white flour</span></li>
         <li><span class="qty">1/2</span> <span class="unit">Cup</span> <span class="name">whole wheat flour</span></li>
         <li><span class="qty">1</span> <span class="unit">tsp</span> <span class="name">salt</span></li>
         <li><span class="qty">1 1/2</span> <span class="unit">tsps</span> <span class="name">baking powder</span></li>
         <li><span class="qty">3/4</span> <span class="unit">Cup</span> <span class="name">sugar</span></li>
         <li><span class="qty">2</span> <span class="unit"></span> <span class="name">eggs</span></li>
         <li><span class="qty">3</span> <span class="unit"></span> <span class="name">very ripe bananas, mashed with a fork until smooth</span></li>
         <li><span class="qty">1</span> <span class="unit">tsp</span> <span class="name">vanilla extract</span></li>
         <li><span class="qty">1/2</span> <span class="unit">Cup</span> <span class="name">chopped walnuts or pecans</span></li>
         <li><span class="qty">1/2</span> <span class="unit">Cup</span> <span class="name">grated dried unsweetened coconut</span></li>
      </ul>
    
    
    
    </div>
    <div id="procedure-header"><h3>Procedure:</h3></div>
    <div id="procedure"><p>Preheat oven to 350&deg;F.  Grease a 9x5 inch loaf pan.<br/>
            Mix together the dry ingredients.  Cream the butter and and beat the eggs and             bananas.  Stir this mixture into the dry ingredients; do not mix more than            necessary.  Gently stir in the vanilla, nuts, and coconut.<br/>
            Pour the batter into the loaf pan and bake for 45 to 60 minutes, until            nicely browned.  A toothpick inserted in the center of the bread will come out            fairly clean when it is done, but because of the bananas this bread will remain            moister than most.  Do not overcook.  Cool on a rack for 15 minutes before             removing from the pan.  To store, wrap in waxed paper. <br/>
            1 Loaf  </p>                        </div>
    <div id="browser-footer">
		  <br/>
		  <hr/>
		  Labels: <a href="index.html#Bread">Bread</a><br/>
	      Last Saved: Sun Jan 17 12:37:09 EST 2010<br/>
	      Created: Sometime before, Sun Jan 17 08:57:23 EST 2010<br/>
	      By: Unknown<br/>
	      Using: <a href="http://code.google.com/p/recipejar/">RecipeJar 5.4 Build 170</a>.
	      <hr/>
         <a href="index.html">Index</a>
      </div>
  </body>
</html>
""",
        ),
        SampleRecipeEntry(
            filename = "Pancakes.html",
            html = """
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="generator" content="RecipeJar"/>
    <meta name="labels" content="Breakfast"/>
    <meta name="last saved" content="Sun Jan 17 12:37:09 EST 2010"/>
    <meta name="created" content="Sometime before, Sun Jan 17 08:57:24 EST 2010"/>
    <meta name="generator" content="RecipeJar"/>
    <title>Pancakes</title>
    <style type="text/css">@import url("style/default.css");</style>
    
  </head>
   <body>
    <div id="header"><h1>Pancakes</h1></div>
    <div id="notes-header"><h3>Notes:</h3></div>
    <div id="notes"><p>From How to Cook Everything      </p>                                                   </div>
    <div id="notes-footer"></div>
    <div id="ingredients-header"><h3>You will need:</h3></div>
    <div id="ingredients">
      <ul>
         <li><span class="qty">2</span> <span class="unit">Cups</span> <span class="name">flour</span></li>
         <li><span class="qty">1</span> <span class="unit">Tbsp</span> <span class="name">baking powder</span></li>
         <li><span class="qty">1/2</span> <span class="unit">tsp</span> <span class="name">salt</span></li>
         <li><span class="qty">1</span> <span class="unit">Tbsp</span> <span class="name">sugar</span></li>
         <li><span class="qty">1-2</span> <span class="unit"></span> <span class="name">eggs</span></li>
         <li><span class="qty">1 1/2-2</span> <span class="unit">Cups</span> <span class="name">milk</span></li>
         <li><span class="qty">3</span> <span class="unit">Tbsps</span> <span class="name">melted butter (to spread on the pancakes), plus some extra for the griddle unless you use oil.</span></li>
      </ul>
    
    
    
    </div>
    <div id="procedure-header"><h3>Procedure:</h3></div>
    <div id="procedure"><p>Mix dry and wet ingredients seperately.  Then mix together, but not too much, a few lumps are okay.</p>                                                                  </div>
    <div id="browser-footer">
		  <br/>
		  <hr/>
		  Labels: <a href="index.html#Breakfast">Breakfast</a><br/>
	      Last Saved: Sun Jan 17 12:37:09 EST 2010<br/>
	      Created: Sometime before, Sun Jan 17 08:57:24 EST 2010<br/>
	      By: Unknown<br/>
	      Using: <a href="http://code.google.com/p/recipejar/">RecipeJar 5.4 Build 170</a>.
	      <hr/>
         <a href="index.html">Index</a>
      </div>
  </body>
</html>
""",
        ),
        SampleRecipeEntry(
            filename = "FrenchToast.html",
            html = """
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="generator" content="RecipeJar"/>
    <meta name="labels" content="Breakfast"/>
    <meta name="author" content="James McConnel"/>
    <meta name="userphone" content=""/>
    <meta name="useremail" content=""/>
    <meta name="custom" content="James McConnel"/>
    <meta name="created" content="Tue Jan 26 10:23:30 EST 2010"/>
    <meta name="last saved" content="Tue Jan 26 10:23:30 EST 2010"/>
    <title>French Toast</title>
    <style type="text/css">@import url("style/default.css");</style>
    
  </head>
   <body>
    <div id="header"><h1>French Toast</h1></div>
    <div id="notes-header"><h3>Notes:</h3></div>
    <div id="notes">Just got lucky.    </div>
    <div id="notes-footer"></div>
    <div id="ingredients-header"><h3>You will need:</h3></div>
    <div id="ingredients">
      <ul>
         <li><span class="qty">1</span> <span class="unit"></span> <span class="name">egg</span></li>
         <li><span class="qty">1/2-1</span> <span class="unit">tsp</span> <span class="name">Cinnamon</span></li>
         <li><span class="qty">1-2</span> <span class="unit">Tbsps</span> <span class="name">Sugar</span></li>
         <li><span class="qty">1/2</span> <span class="unit">tsp</span> <span class="name">Vanilla</span></li>
         <li><span class="qty">1/3</span> <span class="unit">Cup</span> <span class="name">Milk</span></li>
         <li><span class="qty">3-4</span> <span class="unit">Tbsps</span> <span class="name">Melted Butter</span></li>
      </ul>
    </div>
    <div id="procedure-header"><h3>Procedure:</h3></div>
    <div id="procedure">Whip it all together, dunk bread, fry.    </div>
    <div id="browser-footer">
		  <br/>
		  <hr/>
		  Labels: <a href="index.html#Breakfast">Breakfast</a><br/>
	      Last Saved: Tue Jan 26 10:23:30 EST 2010<br/>
	      Created: Tue Jan 26 10:23:30 EST 2010<br/>
	      By: James McConnel<br/>
	      Using: <a href="http://code.google.com/p/recipejar/">RecipeJar 5.4 Build 171</a>.
	      <hr/>
         <a href="index.html">Index</a>
      </div>
  </body>
</html>
""",
        ),
    )

    /** Parse every sample with the real serializer; drop blank titles. */
    fun loadRecipes(): List<Pair<String, Recipe>> =
        entries.mapNotNull { e ->
            val recipe = RecipeSerializer.parse(e.html)
            if (recipe.title.isBlank()) null else e.filename to recipe
        }

    fun listFilenames(): List<String> = loadRecipes().map { it.first }

    fun htmlFor(filename: String): String? =
        entries.firstOrNull { it.filename == filename }?.html

    fun recipeFor(filename: String): Recipe? =
        loadRecipes().firstOrNull { it.first == filename }?.second
}

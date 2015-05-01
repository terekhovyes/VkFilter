package me.alexeyterekhov.vkfilter.Common

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import java.util.Collections
import java.util.HashMap
import java.util.Vector

public object Chef {
    // Spoiled dishes
    val NOT_COOK_ANYMORE = 1
    val COOK_AGAIN_LATER = 2
    val COOK_AGAIN_IMMEDIATELY = 3
    // Cooking attempts
    val UNLIMITED_ATTEMPTS = 0

    fun createRecipe<Ingredient, Dish>() = RecipeCreator(Recipe<Ingredient, Dish>())

    fun cook<Ingredient, Dish>(recipe: Recipe<Ingredient, Dish>, ingredient: Ingredient) {
        cook(recipe, Collections.singleton(ingredient))
    }
    fun cook<Ingredient, Dish>(recipe: Recipe<Ingredient, Dish>, ingredients: Collection<Ingredient>) {
        recipe.cooking addIngredients ingredients
    }

    fun denyCooking(recipe: Recipe<*, *>) = recipe.cooking.deny()
    fun allowCooking(recipe: Recipe<*, *>) = recipe.cooking.allow()
}

class Recipe<Ingredient, Dish> {
    // Options
    var ifCookingFail = Chef.NOT_COOK_ANYMORE
    var waitAfterFail = 0L
    var maxCookAttempts = Chef.UNLIMITED_ATTEMPTS
    // Actions
    var cookAction: ((Ingredient) -> Dish)? = null
    var serveAction: ((Ingredient, Dish) -> Unit)? = null
    var cleanUpAction: ((Ingredient, Exception) -> Unit)? = null
    var finishAction: (() -> Unit)? = null
    // Cooking class
    val cooking = Cooking(this)
}

class RecipeCreator<Ingredient, Dish>(val recipe: Recipe<Ingredient, Dish>) {
    fun create(): Recipe<Ingredient, Dish> {
        if (recipe.cookAction == null)
            throw Exception("Hey! Set cooking action!")
        return recipe
    }
    fun cookThisWay(action: (Ingredient) -> Dish): RecipeCreator<Ingredient, Dish> {
        recipe.cookAction = action
        return this
    }
    fun serveThisWay(action: (Ingredient, Dish) -> Unit): RecipeCreator<Ingredient, Dish> {
        recipe.serveAction = action
        return this
    }
    fun cleanUpThisWay(action: (Ingredient, Exception) -> Unit): RecipeCreator<Ingredient, Dish> {
        recipe.cleanUpAction = action
        return this
    }
    fun finishThisWay(action: () -> Unit): RecipeCreator<Ingredient, Dish> {
        recipe.finishAction = action
        return this
    }
    fun ifCookingFail(cookConstant: Int): RecipeCreator<Ingredient, Dish> {
        recipe.ifCookingFail = cookConstant
        return this
    }
    fun maxCookAttempts(count: Int): RecipeCreator<Ingredient, Dish> {
        recipe.maxCookAttempts = count
        return this
    }
    fun waitAfterCookingFail(milliseconds: Long): RecipeCreator<Ingredient, Dish> {
        recipe.waitAfterFail = milliseconds
        return this
    }
}

class Cooking<Ingredient, Dish>(val recipe: Recipe<Ingredient, Dish>) {
    private val backgroundExecutor = AsyncTask.THREAD_POOL_EXECUTOR
    private val guiHandler = Handler(Looper.getMainLooper())
    private val ingredients = Vector<Ingredient>()
    private var allowCooking = true
    private var cookingRunning = false
    private val attemptCount = HashMap<Ingredient, Int>()

    fun deny() {
        allowCooking = false
    }
    fun allow() {
        allowCooking = true
        startCooking()
    }
    fun addIngredients(i: Collection<Ingredient>) {
        ingredients addAll i
        startCooking()
    }

    private fun startCooking() {
        if (allowCooking && !cookingRunning && ingredients.isNotEmpty()) {
            cookingRunning = true
            backgroundExecutor.execute(backgroundRunnable)
        }
    }
    private fun cookNext() {
        if (allowCooking && ingredients.isNotEmpty()) {
            backgroundExecutor.execute(backgroundRunnable)
        } else {
            cookingRunning = false
            if (ingredients.isEmpty() && recipe.finishAction != null)
                recipe.finishAction!!()
        }
    }

    private val backgroundRunnable = Runnable {
        val ingredient = ingredients remove 0
        try {
            // Try cook, may throw exceptions
            val dish = recipe.cookAction!!(ingredient)
            // Cooked well! Remove info about attempts count
            if (attemptCount containsKey ingredient)
                attemptCount remove ingredient
            // Serve our great dish
            guiHandler post {
                if (recipe.serveAction != null)
                    recipe.serveAction!!(ingredient, dish)
                cookNext()
            }
        } catch (e: Exception) {
            // Oops, dish was spoiled :(
            guiHandler post {
                if (recipe.cleanUpAction != null)
                    recipe.cleanUpAction!!(ingredient, e)
            }
            // Waiting
            if (recipe.waitAfterFail > 0)
                try { Thread.sleep(recipe.waitAfterFail) } catch (e: InterruptedException) {}
            // If chef should cook it again
            if (recipe.ifCookingFail != Chef.NOT_COOK_ANYMORE) {
                // Write our spoiled attempt
                if (recipe.maxCookAttempts != Chef.UNLIMITED_ATTEMPTS) {
                    if (!attemptCount.containsKey(ingredient))
                        attemptCount[ingredient] = 0
                    attemptCount[ingredient] += 1
                }
                // Cook again, if attempts didn't reach maximum value
                if (recipe.maxCookAttempts == Chef.UNLIMITED_ATTEMPTS
                        || recipe.maxCookAttempts > attemptCount[ingredient]) {
                    when (recipe.ifCookingFail) {
                        Chef.COOK_AGAIN_LATER -> ingredients add ingredient
                        Chef.COOK_AGAIN_IMMEDIATELY -> ingredients.add(0, ingredient)
                    }
                }
            }
            guiHandler post { cookNext() }
        }
    }
}
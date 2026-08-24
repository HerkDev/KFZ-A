package de.herk.kfza

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createComposeRule
import de.herk.kfza.ui.KfzaApp
import de.herk.kfza.ui.theme.KFZATheme
import org.junit.Rule
import org.junit.Test

class KfzaAppInputTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun unknownXRemainsVisibleAndKeepsTheNoMatchResult() {
        composeTestRule.setContent {
            KFZATheme { KfzaApp() }
        }

        composeTestRule.onNode(hasSetTextAction()).performTextInput("X")
        composeTestRule.onNodeWithText("X").assertExists()
        composeTestRule.onNodeWithText("No match").assertExists()
        composeTestRule.onAllNodesWithText("—").assertCountEquals(2)
    }
}

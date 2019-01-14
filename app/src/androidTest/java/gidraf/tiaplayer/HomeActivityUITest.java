package gidraf.tiaplayer;


import org.junit.Before;
import org.junit.Rule;

import org.junit.Test;
import org.junit.runner.RunWith;


import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeActivityUITest {

    @Rule
    public ActivityTestRule<HomeActivity> homeActivityActivityTestRule = new ActivityTestRule<>(HomeActivity.class);

    @Before
    public void initValidString() {

    }


    @Test
    public void check_if() {
        onView(withId(R.id.home_layout_appbar))
                .check(matches(isDisplayed()));
    }

    @Test
    public void check_if_home_container_is_displayed() {
        onView(withId(R.id.home_root_layout))
                .check(matches(isDisplayed()));
    }

    @Test
    public void check_if_home_appbar_is_displayed() {
        onView(withId(R.id.home_layout_appbar))
                .check(matches(isDisplayed()));
    }

    @Test
    public void check_if_home_toolbar_is_displayed() {
        onView(withId(R.id.home_toolbar))
                .check(matches(isDisplayed()));
    }

    @Test
    public void check_if_home__is_displayed() {
        onView(withId(R.id.home_layout_collapse_bar))
                .check(matches(isDisplayed()));
    }




}

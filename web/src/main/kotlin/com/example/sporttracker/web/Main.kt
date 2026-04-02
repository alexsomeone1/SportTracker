package com.example.sporttracker.web

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        App()
    }
}

@Composable
fun App() {
    Div({
        style {
            width(100.percent)
            minHeight(100.vh)
            backgroundColor(Color("#f5f5f5"))
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            padding(16.px)
        }
    }) {
        H1({
            style {
                color(Color("#00BCD4"))
                fontSize(32.px)
                margin(0.px, 0.px, 16.px, 0.px)
            }
        }) {
            Text("Sport Tracker")
        }
        P({
            style {
                color(Color("#666"))
                fontSize(16.px)
                margin(0.px)
            }
        }) {
            Text("Веб-версія в розробці. PWA налаштовано.")
        }
    }
}

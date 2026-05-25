import { createBrowserRouter } from "react-router";
import { Root } from "./components/Root";
import { HomePage } from "./pages/HomePage";
import { RegistrationPage } from "./pages/RegistrationPage";
import { ProfilePage } from "./pages/ProfilePage";
import { ClassDetailsPage } from "./pages/ClassDetailsPage";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: Root,
    children: [
      { index: true, Component: HomePage },
      { path: "registration", Component: RegistrationPage },
      { path: "profile", Component: ProfilePage },
      { path: "class/:id", Component: ClassDetailsPage },
    ],
  },
]);

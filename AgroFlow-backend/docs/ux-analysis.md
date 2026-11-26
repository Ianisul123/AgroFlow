# AgroFlow – UX Analysis

## Overview

AgroFlow is a full-stack farm and irrigation-zone management application designed to provide a clear and accessible experience for agricultural users.

The interface focuses on simplicity, data organization, authenticated access, validation, and real-time feedback.

## Target Users

- Farmers
- Greenhouse operators
- Agricultural workers
- Irrigation managers

## Core User Journey

1. Register or log in.
2. Access the farm overview.
3. Create or search for a farm.
4. Open the farm details.
5. Create irrigation zones.
6. Configure crop types, area sizes, and moisture thresholds.
7. Receive system feedback.
8. Log out.

## Implemented UX Features

### Authentication and Authorization

- Dedicated login and registration pages.
- Protected application routes.
- Visible user information and role badges.
- Role-based access control.
- Account-specific farm visibility.
- Administrator access to all farms.

### Farm Management

- Card-based farm overview.
- Search by farm name, location, owner name, or owner email.
- Dedicated farm details page.
- Farm creation and editing.
- Administrator-only farm deletion.

### Irrigation Zones

- Clear forms for irrigation-zone creation.
- Crop type, area size, and moisture threshold configuration.
- Client-side and server-side validation.
- Structured presentation of irrigation-zone information.

### System Feedback

- Loading states for farm retrieval.
- Error messages for failed operations.
- Empty states when no farms are available.
- Real-time notifications through WebSockets and STOMP.
- Automatic farm-list refresh after relevant events.
- Role-based WebSocket subscription authorization.
- Real-time notifications restricted to farm owners
  and administrators.

## Design Decisions

The application uses a green and neutral color palette to reflect its agricultural context.

Card-based layouts improve readability and help users distinguish individual farms and irrigation zones.

Role-based controls reduce unnecessary interactions and communicate which operations are available to each user.

## Testing and Validation

The application includes 62 passing automated backend tests covering authentication, authorization, farm management, irrigation zones, validation, and API integration.

Core user flows were also manually tested through the React interface.

The application includes 73 passing automated backend tests
covering authentication, authorization, farm management,
irrigation zones, validation, API integration, and WebSocket
subscription security.

JaCoCo results:

- Instruction coverage: 86%
- Branch coverage: 69%
- Line coverage: 82%

## Current Limitations

- Further accessibility and responsive design testing is needed.
- Browser-based automated UI tests are not yet implemented.
- Some interface elements may require further refinement.
- No persistent notification history.
- No live moisture sensor integration.
- No automatic irrigation control.

## Future Improvements

- Improve mobile responsiveness.
- Conduct usability testing with representative users.
- Improve keyboard navigation and accessibility.
- Add automated browser-based tests.
- Introduce notification history.
- Integrate real agricultural sensor data.

## Conclusion

AgroFlow provides a functional interface for managing farms and irrigation zones.

Authentication, role-based access, search, validation, and real-time feedback support a clear and structured user experience.

The current implementation provides a foundation for future agricultural management functionality.
